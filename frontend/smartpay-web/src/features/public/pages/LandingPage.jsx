import React from 'react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Logo } from '@/components/common/Logo'
import { ThemeToggle } from '@/components/common/ThemeToggle'
import { ShieldCheck, Zap, Globe, Lock, ArrowRight } from 'lucide-react'

export function LandingPage() {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      {/* Header */}
      <header className="flex items-center justify-between border-b border-slate-200 px-8 py-4 dark:border-slate-800">
        <Logo />
        <div className="flex items-center gap-4">
          <ThemeToggle />
          <Link to="/login">
            <Button variant="ghost">Sign In</Button>
          </Link>
          <Link to="/register">
            <Button>Get Started</Button>
          </Link>
        </div>
      </header>

      {/* Hero */}
      <section className="mx-auto max-w-5xl px-4 py-24 text-center">
        <div className="inline-flex items-center gap-2 rounded-full border border-blue-200 bg-blue-50 px-4 py-1.5 text-xs font-semibold text-blue-700 dark:border-blue-800 dark:bg-blue-950/50 dark:text-blue-300">
          <Zap className="h-3.5 w-3.5" /> Next-Gen FinTech Architecture
        </div>
        <h1 className="mt-6 text-5xl font-extrabold tracking-tight sm:text-6xl text-slate-900 dark:text-white">
          Digital Banking & Wallet Management Platform
        </h1>
        <p className="mx-auto mt-6 max-w-2xl text-lg text-slate-600 dark:text-slate-400">
          Enterprise double-entry ledger, instant wallet transfers, multicurrency support, and bank-grade security.
        </p>
        <div className="mt-10 flex justify-center gap-4">
          <Link to="/register">
            <Button size="lg" className="gap-2">
              Open An Account <ArrowRight className="h-4 w-4" />
            </Button>
          </Link>
          <Link to="/login">
            <Button size="lg" variant="outline">
              Sign In to Portal
            </Button>
          </Link>
        </div>
      </section>

      {/* Feature Grid */}
      <section className="mx-auto max-w-6xl px-4 py-16 grid grid-cols-1 md:grid-cols-3 gap-8">
        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-xs dark:border-slate-800 dark:bg-slate-900">
          <div className="rounded-lg bg-blue-50 p-3 text-blue-600 w-fit dark:bg-blue-950 dark:text-blue-400">
            <ShieldCheck className="h-6 w-6" />
          </div>
          <h3 className="mt-4 font-semibold text-lg">Double-Entry Financial Ledger</h3>
          <p className="mt-2 text-sm text-slate-500">
            Strict debit/credit invariants, immutable journal entries, and cryptographic idempotency keys.
          </p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-xs dark:border-slate-800 dark:bg-slate-900">
          <div className="rounded-lg bg-emerald-50 p-3 text-emerald-600 w-fit dark:bg-emerald-950 dark:text-emerald-400">
            <Globe className="h-6 w-6" />
          </div>
          <h3 className="mt-4 font-semibold text-lg">Multicurrency Wallets</h3>
          <p className="mt-2 text-sm text-slate-500">
            Hold, transfer, and exchange USD, EUR, GBP, and INR with sub-second clearance and real-time ledger projections.
          </p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-xs dark:border-slate-800 dark:bg-slate-900">
          <div className="rounded-lg bg-purple-50 p-3 text-purple-600 w-fit dark:bg-purple-950 dark:text-purple-400">
            <Lock className="h-6 w-6" />
          </div>
          <h3 className="mt-4 font-semibold text-lg">Bank-Grade Microservices</h3>
          <p className="mt-2 text-sm text-slate-500">
            9 decoupled Spring Boot microservices backed by MySQL, Redis cache, Kafka KRaft, and RabbitMQ.
          </p>
        </div>
      </section>
    </div>
  )
}
