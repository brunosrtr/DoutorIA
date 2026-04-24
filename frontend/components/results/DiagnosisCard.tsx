'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { ChevronDown, ChevronUp } from 'lucide-react';
import type { DiagnosisResponse } from '@/types/api';

const GRAVIDADE_CONFIG = {
  baixa: { label: 'Baixa', className: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 border-green-200 dark:border-green-700' },
  media: { label: 'Média', className: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200 border-yellow-200 dark:border-yellow-700' },
  alta: { label: 'Alta', className: 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200 border-orange-200 dark:border-orange-700' },
  critica: { label: 'Crítica', className: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 border-red-200 dark:border-red-700' },
};

interface DiagnosisCardProps {
  diagnosis: DiagnosisResponse;
}

export default function DiagnosisCard({ diagnosis }: DiagnosisCardProps) {
  const [expanded, setExpanded] = useState(false);
  const config = GRAVIDADE_CONFIG[diagnosis.gravidade] ?? GRAVIDADE_CONFIG.media;
  const confidence = diagnosis.confiancaPercentual ?? 0;

  return (
    <Card className="overflow-hidden">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-1">
            <CardTitle className="text-base leading-tight">{diagnosis.nomeDiagnostico}</CardTitle>
            {diagnosis.cidCodigo && (
              <p className="text-xs text-muted-foreground">CID: {diagnosis.cidCodigo}</p>
            )}
          </div>
          <Badge className={`shrink-0 border ${config.className}`}>{config.label}</Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="space-y-1">
          <div className="flex justify-between text-xs text-muted-foreground">
            <span>Confiança</span>
            <span>{confidence.toFixed(1)}%</span>
          </div>
          <Progress value={confidence} className="h-2" />
        </div>

        {diagnosis.justificativa && (
          <div>
            <button
              className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground transition-colors"
              onClick={() => setExpanded(!expanded)}
            >
              {expanded ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />}
              Justificativa
            </button>
            {expanded && (
              <p className="mt-2 text-sm text-muted-foreground leading-relaxed">
                {diagnosis.justificativa}
              </p>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
