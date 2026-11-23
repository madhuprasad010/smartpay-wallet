package com.smartpay;

import com.smartpay.beneficiary.dto.BeneficiaryDto;
import com.smartpay.beneficiary.dto.CreateBeneficiaryRequest;
import com.smartpay.beneficiary.service.BeneficiaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BeneficiaryServiceTests {

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Test
    @DisplayName("Beneficiary: Create, retrieve, and delete beneficiary")
    void testBeneficiaryLifecycle() {
        CreateBeneficiaryRequest request = new CreateBeneficiaryRequest(
                1101L,
                "Priya Sharma",
                "987654321012",
                "HDFC0001234",
                "HDFC Bank",
                "priya@example.com"
        );

        BeneficiaryDto created = beneficiaryService.createBeneficiary(request);
        assertNotNull(created);
        assertEquals("Priya Sharma", created.name());
        assertEquals("VERIFIED", created.status());

        List<BeneficiaryDto> userList = beneficiaryService.getByUserId(1101L);
        assertFalse(userList.isEmpty());
        assertEquals(1, userList.size());

        // Delete
        beneficiaryService.deleteBeneficiary(created.id());
        List<BeneficiaryDto> afterDelete = beneficiaryService.getByUserId(1101L);
        assertTrue(afterDelete.isEmpty());
    }
}
