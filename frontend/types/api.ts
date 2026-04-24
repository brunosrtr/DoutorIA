export type SexoEnum = 'masculino' | 'feminino' | 'outro' | 'nao_informado';
export type AssessmentStatusEnum = 'rascunho' | 'processando' | 'concluido' | 'erro';
export type OcrStatusEnum = 'pendente' | 'processado' | 'erro';
export type GravidadeEnum = 'baixa' | 'media' | 'alta' | 'critica';
export type SugestaoTipoEnum = 'especialista' | 'exame' | 'habito' | 'urgencia';

export interface PatientResponse {
  id: string;
  nome: string;
  dataNascimento: string | null;
  sexo: SexoEnum;
  tipoSanguineo: string | null;
  alergiasConhecidas: string[];
  createdAt: string;
}

export interface AssessmentResponse {
  id: string;
  patientId: string;
  status: AssessmentStatusEnum;
  createdAt: string;
}

export interface SuggestionResponse {
  id: string;
  diagnosticId: string;
  tipo: SugestaoTipoEnum;
  descricao: string;
  prioridade: number;
}

export interface DiagnosisResponse {
  id: string;
  nomeDiagnostico: string;
  cidCodigo: string | null;
  confiancaPercentual: number | null;
  gravidade: GravidadeEnum;
  justificativa: string;
  sugestoes: SuggestionResponse[];
}

export interface FollowupQuestionResponse {
  pergunta: string;
  categoria: string | null;
  contexto: string | null;
  resposta: string | null;
}

export interface AssessmentResultResponse {
  assessmentId: string;
  status: AssessmentStatusEnum;
  urgente: boolean | null;
  resumoGeral: string | null;
  message: string | null;
  diagnosticos: DiagnosisResponse[] | null;
  sugestoes: SuggestionResponse[] | null;
  relatorios: ReportResponse[] | null;
  perguntas: FollowupQuestionResponse[] | null;
  precisaMaisInfo: boolean | null;
}

export interface HealthImprovementPoint {
  titulo: string;
  descricao: string;
  categoria: string;
  prioridade: 'alta' | 'media' | 'baixa';
}

export interface HealthSummaryResponse {
  pontosMelhoria: HealthImprovementPoint[];
  resumo: string | null;
  geradoEm: string | null;
  totalAvaliacoes: number;
  semDados: boolean;
}

export interface AnswerItem {
  pergunta: string;
  resposta: string;
}

export interface SubmitAnswersRequest {
  respostas: AnswerItem[];
}

export interface SymptomCatalogItem {
  id: string;
  nome: string;
  categoria: string;
  sinonimos: string[];
}

export interface AssessmentSummaryResponse {
  id: string;
  patientId: string;
  patientNome: string;
  status: AssessmentStatusEnum;
  urgente: boolean;
  totalSintomas: number;
  totalDiagnosticos: number;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface AddSymptomsResponse {
  assessmentId: string;
  sintomasAdicionados: number;
}

export interface CreatePatientRequest {
  nome: string;
  dataNascimento?: string;
  sexo?: SexoEnum;
  tipoSanguineo?: string;
  alergiasConhecidas?: string[];
}

export interface SymptomItemRequest {
  symptomId: string | null;
  nomeCustom?: string;
  intensidade: number;
  duracaoDias?: number;
  localizacaoCorpo?: string;
  observacoes?: string;
}

export interface ReportResponse {
  id: string;
  assessmentId: string;
  fileName: string;
  fileType: string;
  fileSizeBytes: number;
  ocrStatus: OcrStatusEnum;
  createdAt: string;
}
