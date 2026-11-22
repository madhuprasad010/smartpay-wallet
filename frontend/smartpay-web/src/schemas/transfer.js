import { z } from 'zod'

export const transferSchema = z.object({
  sourceWalletId: z.number().positive(),
  destWalletId: z.number().positive(),
  amount: z.number().positive('Amount must be greater than zero'),
  currency: z.string().default('USD'),
  description: z.string().optional()
})
