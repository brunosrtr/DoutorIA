'use client';

import { useParams, useRouter } from 'next/navigation';
import { useAssessmentResult } from '@/lib/api';
import { Skeleton } from '@/components/ui/skeleton';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { Loader2, ArrowLeft, FileText, ImageIcon } from 'lucide-react';
import MedicalDisclaimer from '@/components/results/MedicalDisclaimer';
import UrgencyBanner from '@/components/results/UrgencyBanner';
import DiagnosisCard from '@/components/results/DiagnosisCard';
import SuggestionList from '@/components/results/SuggestionList';
import FollowupQuestions from '@/components/results/FollowupQuestions';
import type { ReportResponse } from '@/types/api';

function ocrStatusLabel(status: string) {
  if (status === 'processado') return { label: 'Processado', className: 'border-green-500 text-green-700 dark:text-green-400' };
  if (status === 'erro') return { label: 'Erro', className: 'border-red-500 text-red-700 dark:text-red-400' };
  return { label: 'Pendente', className: 'border-yellow-500 text-yellow-700 dark:text-yellow-400' };
}

function ReportItem({ report }: { report: ReportResponse }) {
  const isPdf = report.fileType === 'application/pdf';
  const ocr = ocrStatusLabel(report.ocrStatus);
  return (
    <div className="flex items-center gap-3 p-3 border rounded-lg text-sm">
      {isPdf
        ? <FileText className="h-5 w-5 text-red-500 shrink-0" />
        : <ImageIcon className="h-5 w-5 text-blue-500 shrink-0" />}
      <div className="flex-1 min-w-0">
        <p className="font-medium truncate">{report.fileName}</p>
        <p className="text-xs text-muted-foreground">
          {isPdf ? 'PDF' : 'Imagem'} · {(report.fileSizeBytes / 1024).toFixed(0)} KB
        </p>
      </div>
      <Badge variant="outline" className={`shrink-0 text-xs ${ocr.className}`}>
        {ocr.label}
      </Badge>
    </div>
  );
}

export default function AssessmentResultPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { data, isLoading, error } = useAssessmentResult(id);

  if (isLoading) {
    return (
      <div className="max-w-2xl mx-auto py-8 px-4 space-y-4">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-16 w-full" />
        <Skeleton className="h-32 w-full" />
        <Skeleton className="h-32 w-full" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-2xl mx-auto py-8 px-4">
        <p className="text-destructive">Erro ao carregar resultado. Tente novamente.</p>
        <Button variant="outline" onClick={() => router.back()} className="mt-4">
          Voltar
        </Button>
      </div>
    );
  }

  if (!data) return null;

  const awaitingAnswers = data.precisaMaisInfo && (data.perguntas?.length ?? 0) > 0;

  if (data.status === 'processando' && !awaitingAnswers) {
    return (
      <div className="max-w-2xl mx-auto py-8 px-4 flex flex-col items-center gap-4 py-20">
        <Loader2 className="h-10 w-10 animate-spin text-primary" />
        <p className="text-lg font-medium">Analisando com IA...</p>
        <p className="text-muted-foreground text-sm text-center">
          Isso pode levar alguns segundos. A página será atualizada automaticamente.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto py-8 px-4 space-y-4">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" onClick={() => router.push('/assessments')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-xl font-bold">
            {awaitingAnswers ? 'Análise em andamento' : 'Resultado da Avaliação'}
          </h1>
          <Badge
            variant="outline"
            className={
              awaitingAnswers
                ? 'border-primary text-primary'
                : data.status === 'concluido'
                ? 'border-green-500 text-green-700 dark:text-green-400'
                : 'border-red-500 text-red-700 dark:text-red-400'
            }
          >
            {awaitingAnswers ? 'aguardando respostas' : data.status}
          </Badge>
        </div>
      </div>

      {/* Disclaimer — always first */}
      <MedicalDisclaimer />

      {/* Follow-up questions — priority when awaiting answers */}
      {awaitingAnswers && (
        <FollowupQuestions assessmentId={data.assessmentId} perguntas={data.perguntas ?? []} />
      )}

      {/* Urgency banner if urgent */}
      {!awaitingAnswers && <UrgencyBanner urgente={data.urgente ?? false} />}

      {/* General summary */}
      {data.resumoGeral && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Resumo Geral</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm leading-relaxed text-muted-foreground">{data.resumoGeral}</p>
          </CardContent>
        </Card>
      )}

      {/* Diagnoses */}
      {data.diagnosticos && data.diagnosticos.length > 0 && (
        <div className="space-y-3">
          <h2 className="font-semibold text-sm uppercase tracking-wide text-muted-foreground">
            Diagnósticos Possíveis ({data.diagnosticos.length})
          </h2>
          {[...data.diagnosticos]
            .sort((a, b) => (b.confiancaPercentual ?? 0) - (a.confiancaPercentual ?? 0))
            .map((d) => (
              <DiagnosisCard key={d.id} diagnosis={d} />
            ))}
        </div>
      )}

      {/* Suggestions */}
      {data.sugestoes && data.sugestoes.length > 0 && (
        <>
          <Separator />
          <div className="space-y-3">
            <h2 className="font-semibold text-sm uppercase tracking-wide text-muted-foreground">
              Sugestões
            </h2>
            <SuggestionList suggestions={data.sugestoes} />
          </div>
        </>
      )}

      {/* Documents analyzed */}
      {data.relatorios && data.relatorios.length > 0 && (
        <>
          <Separator />
          <div className="space-y-3">
            <h2 className="font-semibold text-sm uppercase tracking-wide text-muted-foreground">
              Documentos Analisados ({data.relatorios.length})
            </h2>
            <div className="space-y-2">
              {data.relatorios.map((r) => (
                <ReportItem key={r.id} report={r} />
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
