'use client';

import Link from 'next/link';
import { Plus, ClipboardList, Stethoscope, Activity, ShieldCheck } from 'lucide-react';
import { buttonVariants } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';
import { useAssessmentHistory, useProfile, useDeleteAssessment } from '@/lib/api';
import { AssessmentCard } from '@/components/assessment/AssessmentCard';
import HealthSummaryCard from '@/components/dashboard/HealthSummaryCard';

export default function DashboardPage() {
  const { data: profile } = useProfile();
  const { data: historyPage, isLoading: historyLoading } = useAssessmentHistory(0);
  const deleteMutation = useDeleteAssessment();

  const recentAssessments = historyPage?.content.slice(0, 3) ?? [];

  function handleDelete(id: string) {
    if (confirm('Excluir esta avaliação permanentemente?')) {
      deleteMutation.mutate(id);
    }
  }

  return (
    <div className="max-w-3xl mx-auto py-8 px-4 space-y-8">
      <div className="space-y-1">
        <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-primary/10 text-primary text-xs font-medium">
          <ShieldCheck className="h-3 w-3" /> Assistente pessoal de saúde
        </div>
        <h1 className="text-3xl font-bold tracking-tight mt-3">
          Olá{profile?.nome ? `, ${profile.nome.split(' ')[0]}` : ''}
        </h1>
        <p className="text-muted-foreground text-sm">
          Descreva seus sintomas e receba uma análise com IA — incluindo perguntas de acompanhamento para refinar o diagnóstico
        </p>
      </div>

      <Card className="relative overflow-hidden border-0 ring-0 bg-gradient-to-br from-primary via-primary to-[oklch(0.4_0.12_215)] text-primary-foreground shadow-lg shadow-primary/20">
        <div
          aria-hidden
          className="absolute inset-0 opacity-[0.08] bg-[radial-gradient(circle_at_85%_15%,white,transparent_55%)]"
        />
        <CardHeader className="relative">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-lg bg-white/15 backdrop-blur-sm">
              <Stethoscope className="h-5 w-5" />
            </div>
            <CardTitle className="text-primary-foreground text-lg">Nova Avaliação</CardTitle>
          </div>
          <CardDescription className="text-primary-foreground/80 max-w-md">
            Descreva sintomas, anexe exames e receba uma análise preliminar com diagnósticos sugeridos e encaminhamentos
          </CardDescription>
        </CardHeader>
        <CardContent className="relative">
          <Link
            href="/new-assessment"
            className={cn(
              buttonVariants({ size: 'lg', variant: 'secondary' }),
              'w-full sm:w-auto bg-white text-primary hover:bg-white/90 shadow-sm'
            )}
          >
            <Plus className="h-4 w-4 mr-2" /> Iniciar Nova Avaliação
          </Link>
        </CardContent>
      </Card>

      <Card className="transition-shadow hover:shadow-md">
        <CardHeader className="pb-3 flex-row items-center justify-between space-y-0">
          <CardTitle className="text-sm font-medium text-muted-foreground">Avaliações</CardTitle>
          <div className="p-1.5 rounded-md bg-primary/10 text-primary">
            <Activity className="h-4 w-4" />
          </div>
        </CardHeader>
        <CardContent>
          <p className="text-3xl font-semibold tracking-tight">{historyPage?.totalElements ?? 0}</p>
          <p className="text-xs text-muted-foreground mt-1">realizadas no total</p>
        </CardContent>
      </Card>

      <HealthSummaryCard />

      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="font-semibold">Últimas Avaliações</h2>
          <Link href="/assessments" className="text-sm text-muted-foreground hover:text-foreground">
            Ver todas
          </Link>
        </div>

        {historyLoading ? (
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-20 w-full rounded-xl" />
            ))}
          </div>
        ) : recentAssessments.length === 0 ? (
          <div className="flex flex-col items-center gap-3 py-8 text-center">
            <ClipboardList className="h-8 w-8 text-muted-foreground" />
            <p className="text-sm text-muted-foreground">Nenhuma avaliação ainda</p>
          </div>
        ) : (
          <div className="space-y-3">
            {recentAssessments.map((a) => (
              <AssessmentCard
                key={a.id}
                assessment={a}
                onDelete={handleDelete}
                deleting={deleteMutation.isPending}
              />
            ))}
            {(historyPage?.totalElements ?? 0) > 3 && (
              <Link
                href="/assessments"
                className={cn(buttonVariants({ variant: 'outline' }), 'w-full')}
              >
                <ClipboardList className="h-4 w-4 mr-2" /> Ver todas as avaliações
              </Link>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
