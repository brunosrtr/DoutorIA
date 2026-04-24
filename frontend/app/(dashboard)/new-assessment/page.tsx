'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import {
  useProfile,
  useCreateAssessment,
  useAddSymptoms,
  useSubmitAssessment,
  useUploadReport,
} from '@/lib/api';
import SymptomSearch from '@/components/assessment/SymptomSearch';
import IntensitySlider from '@/components/assessment/IntensitySlider';
import BodyLocationSelect from '@/components/assessment/BodyLocationSelect';
import FileUploader from '@/components/assessment/FileUploader';
import MedicalDisclaimer from '@/components/results/MedicalDisclaimer';
import type { SymptomCatalogItem, SymptomItemRequest } from '@/types/api';
import { Loader2, Plus, X, ChevronRight, ChevronLeft, AlertCircle } from 'lucide-react';

interface AddedSymptom {
  symptomId: string | null;
  nome: string;
  intensidade: number;
  duracaoDias: number;
  localizacaoCorpo: string;
  observacoes: string;
  custom: boolean;
}

interface FileItem {
  file: File;
  id: string;
  error?: string;
}

const STEP_LABELS = ['Sintomas', 'Exames', 'Revisão'];

export default function NewAssessmentPage() {
  const router = useRouter();
  const [step, setStep] = useState(1);
  const [assessmentId, setAssessmentId] = useState<string | null>(null);
  const [symptoms, setSymptoms] = useState<AddedSymptom[]>([]);
  const [files, setFiles] = useState<FileItem[]>([]);
  const [uploadWarnings, setUploadWarnings] = useState<string[]>([]);
  const [currentSymptom, setCurrentSymptom] = useState<Partial<AddedSymptom>>({
    intensidade: 5,
    duracaoDias: 1,
    localizacaoCorpo: '',
    observacoes: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { data: profile } = useProfile();
  const createAssessment = useCreateAssessment();
  const addSymptoms = useAddSymptoms();
  const submitAssessment = useSubmitAssessment();
  const uploadReport = useUploadReport();

  function handleSymptomSelect(item: SymptomCatalogItem | { id: null; nome: string; custom: true }) {
    setCurrentSymptom((prev) => ({
      ...prev,
      symptomId: item.id,
      nome: item.nome,
      custom: 'custom' in item,
    }));
  }

  function addCurrentSymptom() {
    if (!currentSymptom.nome) return;
    setSymptoms((prev) => [...prev, currentSymptom as AddedSymptom]);
    setCurrentSymptom({ intensidade: 5, duracaoDias: 1, localizacaoCorpo: '', observacoes: '' });
  }

  async function handleGoToUpload() {
    if (!profile?.id || symptoms.length === 0) return;
    setError(null);
    try {
      let aId = assessmentId;
      if (!aId) {
        const a = await createAssessment.mutateAsync(profile.id);
        aId = a.id;
        setAssessmentId(aId);
      }
      const symptomItems: SymptomItemRequest[] = symptoms.map((s) => ({
        symptomId: s.symptomId,
        nomeCustom: s.custom ? s.nome : undefined,
        intensidade: s.intensidade,
        duracaoDias: s.duracaoDias || undefined,
        localizacaoCorpo: s.localizacaoCorpo || undefined,
        observacoes: s.observacoes || undefined,
      }));
      await addSymptoms.mutateAsync({ assessmentId: aId!, sintomas: symptomItems });
      setStep(2);
    } catch (e: unknown) {
      setError((e as Error).message || 'Erro ao salvar sintomas');
    }
  }

  async function handleSubmit() {
    if (!assessmentId) return;
    setSubmitting(true);
    setError(null);
    setUploadWarnings([]);

    const validFiles = files.filter((f) => !f.error);
    const warnings: string[] = [];

    for (const item of validFiles) {
      try {
        await uploadReport.mutateAsync({ assessmentId, file: item.file });
      } catch {
        warnings.push(`Falha ao enviar "${item.file.name}" — a análise continuará sem este arquivo`);
      }
    }

    if (warnings.length > 0) {
      setUploadWarnings(warnings);
    }

    try {
      const result = await submitAssessment.mutateAsync(assessmentId);
      router.push(`/assessments/${result.assessmentId}`);
    } catch (e: unknown) {
      setError((e as Error).message || 'Erro ao processar avaliação');
      setSubmitting(false);
    }
  }

  return (
    <div className="max-w-2xl mx-auto py-8 px-4 space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Nova Avaliação</h1>
        <p className="text-muted-foreground text-sm mt-1">
          Passo {step} de {STEP_LABELS.length} — {STEP_LABELS[step - 1]}
        </p>
      </div>

      {/* Step 1: Symptoms */}
      {step === 1 && (
        <div className="space-y-4">
          <Card className="overflow-visible">
            <CardHeader>
              <CardTitle>Adicionar Sintomas</CardTitle>
              <CardDescription>Descreva o que você está sentindo</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label>Sintoma</Label>
                <SymptomSearch onSelect={handleSymptomSelect} />
                {currentSymptom.nome && (
                  <Badge variant="secondary">{currentSymptom.nome}</Badge>
                )}
              </div>

              {currentSymptom.nome && (
                <>
                  <IntensitySlider
                    label="Intensidade"
                    value={currentSymptom.intensidade ?? 5}
                    onChange={(v) => setCurrentSymptom((p) => ({ ...p, intensidade: v }))}
                  />
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label>Duração (dias)</Label>
                      <Input
                        type="number"
                        min={0}
                        value={currentSymptom.duracaoDias ?? ''}
                        onChange={(e) => setCurrentSymptom((p) => ({ ...p, duracaoDias: +e.target.value }))}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>Localização</Label>
                      <BodyLocationSelect
                        value={currentSymptom.localizacaoCorpo ?? ''}
                        onChange={(v) => setCurrentSymptom((p) => ({ ...p, localizacaoCorpo: v }))}
                      />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label>Observações</Label>
                    <Input
                      placeholder="Ex: piora à noite, melhora com repouso..."
                      value={currentSymptom.observacoes ?? ''}
                      onChange={(e) => setCurrentSymptom((p) => ({ ...p, observacoes: e.target.value }))}
                    />
                  </div>
                  <Button onClick={addCurrentSymptom} variant="outline" className="w-full">
                    <Plus className="h-4 w-4 mr-2" /> Adicionar Sintoma
                  </Button>
                </>
              )}
            </CardContent>
          </Card>

          {symptoms.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Sintomas adicionados ({symptoms.length})</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                {symptoms.map((s, i) => (
                  <div key={i} className="flex items-center justify-between p-2 border rounded-lg">
                    <div className="space-y-0.5">
                      <p className="text-sm font-medium">{s.nome}</p>
                      <p className="text-xs text-muted-foreground">
                        Intensidade: {s.intensidade}/10
                        {s.localizacaoCorpo && ` · ${s.localizacaoCorpo}`}
                      </p>
                    </div>
                    <button onClick={() => setSymptoms((p) => p.filter((_, j) => j !== i))}>
                      <X className="h-4 w-4 text-muted-foreground hover:text-destructive" />
                    </button>
                  </div>
                ))}
              </CardContent>
            </Card>
          )}

          {error && (
            <div className="p-3 bg-destructive/10 border border-destructive/30 rounded-lg text-sm text-destructive">
              {error}
            </div>
          )}

          <div className="flex gap-3">
            <Button
              onClick={handleGoToUpload}
              disabled={symptoms.length === 0 || addSymptoms.isPending || createAssessment.isPending}
              className="flex-1"
            >
              {(addSymptoms.isPending || createAssessment.isPending) && (
                <Loader2 className="h-4 w-4 animate-spin mr-2" />
              )}
              Próximo <ChevronRight className="h-4 w-4 ml-1" />
            </Button>
          </div>
        </div>
      )}

      {/* Step 2: File Upload */}
      {step === 2 && (
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Exames e Documentos</CardTitle>
              <CardDescription>
                Opcional — anexe fotos de exames ou PDFs de relatórios médicos para enriquecer a análise
              </CardDescription>
            </CardHeader>
            <CardContent>
              <FileUploader files={files} onChange={setFiles} />
            </CardContent>
          </Card>

          <div className="flex gap-3">
            <Button variant="outline" onClick={() => setStep(1)}>
              <ChevronLeft className="h-4 w-4 mr-1" /> Voltar
            </Button>
            <Button onClick={() => setStep(3)} className="flex-1">
              {files.filter((f) => !f.error).length > 0
                ? `Continuar com ${files.filter((f) => !f.error).length} arquivo(s)`
                : 'Continuar sem arquivos'}
              <ChevronRight className="h-4 w-4 ml-1" />
            </Button>
          </div>
        </div>
      )}

      {/* Step 3: Review & Submit */}
      {step === 3 && (
        <div className="space-y-4">
          <MedicalDisclaimer />
          <Card>
            <CardHeader>
              <CardTitle>Revisão</CardTitle>
              <CardDescription>Confirme os dados antes de enviar para análise</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-sm font-medium mb-2">Paciente</p>
                <p className="text-sm text-muted-foreground">{profile?.nome}</p>
              </div>
              <Separator />
              <div>
                <p className="text-sm font-medium mb-2">Sintomas ({symptoms.length})</p>
                <ul className="space-y-1">
                  {symptoms.map((s, i) => (
                    <li key={i} className="text-sm text-muted-foreground flex items-center gap-2">
                      <span className="h-1.5 w-1.5 rounded-full bg-primary shrink-0" />
                      {s.nome} — intensidade {s.intensidade}/10
                    </li>
                  ))}
                </ul>
              </div>
              {files.filter((f) => !f.error).length > 0 && (
                <>
                  <Separator />
                  <div>
                    <p className="text-sm font-medium mb-2">
                      Arquivos ({files.filter((f) => !f.error).length})
                    </p>
                    <ul className="space-y-1">
                      {files.filter((f) => !f.error).map((item) => (
                        <li key={item.id} className="text-sm text-muted-foreground flex items-center gap-2">
                          <span className="h-1.5 w-1.5 rounded-full bg-primary shrink-0" />
                          {item.file.name}
                        </li>
                      ))}
                    </ul>
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          {uploadWarnings.length > 0 && (
            <div className="space-y-1 p-3 bg-yellow-50 dark:bg-yellow-950/30 border border-yellow-200 dark:border-yellow-800 rounded-lg">
              {uploadWarnings.map((w, i) => (
                <div key={i} className="flex items-start gap-2 text-sm text-yellow-800 dark:text-yellow-200">
                  <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" />
                  {w}
                </div>
              ))}
            </div>
          )}

          {error && (
            <div className="p-3 bg-destructive/10 border border-destructive/30 rounded-lg text-sm text-destructive">
              {error}
            </div>
          )}

          <div className="flex gap-3">
            <Button variant="outline" onClick={() => setStep(2)} disabled={submitting}>
              <ChevronLeft className="h-4 w-4 mr-1" /> Voltar
            </Button>
            <Button onClick={handleSubmit} disabled={submitting} className="flex-1">
              {submitting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin mr-2" />
                  Analisando com IA...
                </>
              ) : (
                'Analisar com IA'
              )}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
