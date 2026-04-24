'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useAssessmentHistory, useDeleteAssessment } from '@/lib/api';
import { AssessmentCard } from '@/components/assessment/AssessmentCard';
import { buttonVariants } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Plus, ClipboardList, ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

export default function AssessmentsPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useAssessmentHistory(page);
  const deleteMutation = useDeleteAssessment();

  const assessments = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  function handleDelete(id: string) {
    if (confirm('Excluir esta avaliação permanentemente?')) {
      deleteMutation.mutate(id);
    }
  }

  return (
    <div className="max-w-2xl mx-auto py-8 px-4 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Histórico de Avaliações</h1>
        <Link href="/new-assessment" className={buttonVariants()}>
          <Plus className="h-4 w-4 mr-2" /> Nova Avaliação
        </Link>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-20 w-full rounded-xl" />
          ))}
        </div>
      ) : assessments.length === 0 ? (
        <div className="flex flex-col items-center gap-4 py-16 text-center">
          <ClipboardList className="h-12 w-12 text-muted-foreground" />
          <p className="text-muted-foreground">Nenhuma avaliação encontrada</p>
          <Link href="/new-assessment" className={cn(buttonVariants())}>
            <Plus className="h-4 w-4 mr-2" /> Iniciar Nova Avaliação
          </Link>
        </div>
      ) : (
        <>
          <div className="space-y-3">
            {assessments.map((a) => (
              <AssessmentCard
                key={a.id}
                assessment={a}
                onDelete={handleDelete}
                deleting={deleteMutation.isPending}
              />
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2">
              <Button
                variant="outline"
                size="icon"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <span className="text-sm text-muted-foreground">
                Página {page + 1} de {totalPages}
              </span>
              <Button
                variant="outline"
                size="icon"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
