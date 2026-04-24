'use client';

import Link from 'next/link';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Trash2, ChevronRight, AlertTriangle } from 'lucide-react';
import type { AssessmentSummaryResponse } from '@/types/api';

const STATUS_LABELS: Record<string, string> = {
  rascunho: 'Rascunho',
  processando: 'Processando',
  concluido: 'Concluído',
  erro: 'Erro',
};

const STATUS_VARIANT: Record<string, 'default' | 'secondary' | 'destructive' | 'outline'> = {
  rascunho: 'secondary',
  processando: 'outline',
  concluido: 'default',
  erro: 'destructive',
};

interface Props {
  assessment: AssessmentSummaryResponse;
  onDelete: (id: string) => void;
  deleting?: boolean;
}

export function AssessmentCard({ assessment, onDelete, deleting }: Props) {
  const date = new Date(assessment.createdAt).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });

  return (
    <Card className="group relative">
      <CardContent className="flex items-center gap-4 p-4">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="font-medium truncate">{assessment.patientNome}</span>
            {assessment.urgente && (
              <Badge variant="destructive" className="gap-1">
                <AlertTriangle className="h-3 w-3" /> Urgente
              </Badge>
            )}
            <Badge variant={STATUS_VARIANT[assessment.status]}>
              {STATUS_LABELS[assessment.status] ?? assessment.status}
            </Badge>
          </div>
          <p className="text-sm text-muted-foreground mt-0.5">
            {assessment.totalSintomas} sintoma{assessment.totalSintomas !== 1 ? 's' : ''}
            {assessment.totalDiagnosticos > 0 && ` · ${assessment.totalDiagnosticos} diagnóstico${assessment.totalDiagnosticos !== 1 ? 's' : ''}`}
            {' · '}{date}
          </p>
        </div>

        <div className="flex items-center gap-1 shrink-0">
          <Button
            variant="ghost"
            size="icon"
            className="text-muted-foreground hover:text-destructive opacity-0 group-hover:opacity-100 transition-opacity"
            onClick={() => onDelete(assessment.id)}
            disabled={deleting}
            aria-label="Excluir avaliação"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
          {assessment.status === 'concluido' && (
            <Link
              href={`/assessments/${assessment.id}`}
              className="inline-flex items-center justify-center h-8 w-8 rounded-md hover:bg-accent transition-colors"
              aria-label="Ver resultado"
            >
              <ChevronRight className="h-4 w-4" />
            </Link>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
