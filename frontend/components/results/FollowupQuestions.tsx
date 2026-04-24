'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { Loader2, Sparkles } from 'lucide-react';
import { useSubmitAnswers } from '@/lib/api';
import type { FollowupQuestionResponse } from '@/types/api';

interface Props {
  assessmentId: string;
  perguntas: FollowupQuestionResponse[];
}

export default function FollowupQuestions({ assessmentId, perguntas }: Props) {
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const submitAnswers = useSubmitAnswers();

  const allAnswered = perguntas.every((q) => (answers[q.pergunta] ?? '').trim().length > 0);

  async function handleSubmit() {
    setError(null);
    try {
      await submitAnswers.mutateAsync({
        assessmentId,
        respostas: perguntas.map((q) => ({
          pergunta: q.pergunta,
          resposta: (answers[q.pergunta] ?? '').trim(),
        })),
      });
    } catch (e: unknown) {
      setError((e as Error).message || 'Erro ao enviar respostas');
    }
  }

  return (
    <Card className="border-primary/40 bg-primary/5">
      <CardHeader>
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-md bg-primary/15 text-primary">
            <Sparkles className="h-4 w-4" />
          </div>
          <CardTitle className="text-base">A IA precisa de mais detalhes</CardTitle>
        </div>
        <CardDescription>
          Responda às perguntas abaixo para que a análise fique mais precisa.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-5">
        {perguntas.map((q, i) => (
          <div key={i} className="space-y-2">
            <div className="space-y-1">
              <Label className="text-sm font-medium leading-snug">
                {i + 1}. {q.pergunta}
              </Label>
              {q.categoria && (
                <Badge variant="outline" className="text-[10px]">
                  {q.categoria}
                </Badge>
              )}
              {q.contexto && (
                <p className="text-xs text-muted-foreground">{q.contexto}</p>
              )}
            </div>
            <Textarea
              rows={2}
              placeholder="Sua resposta..."
              value={answers[q.pergunta] ?? ''}
              onChange={(e) =>
                setAnswers((prev) => ({ ...prev, [q.pergunta]: e.target.value }))
              }
            />
          </div>
        ))}

        {error && (
          <div className="p-3 bg-destructive/10 border border-destructive/30 rounded-lg text-sm text-destructive">
            {error}
          </div>
        )}

        <Button
          onClick={handleSubmit}
          disabled={!allAnswered || submitAnswers.isPending}
          className="w-full"
        >
          {submitAnswers.isPending && <Loader2 className="h-4 w-4 animate-spin mr-2" />}
          Enviar respostas e continuar análise
        </Button>
      </CardContent>
    </Card>
  );
}
