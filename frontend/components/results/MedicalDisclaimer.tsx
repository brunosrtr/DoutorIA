import { Alert, AlertDescription } from '@/components/ui/alert';
import { Info } from 'lucide-react';

export default function MedicalDisclaimer() {
  return (
    <Alert className="border-yellow-400 bg-yellow-50 dark:bg-yellow-950/30 dark:border-yellow-700">
      <Info className="h-4 w-4 text-yellow-600 dark:text-yellow-400" />
      <AlertDescription className="text-yellow-800 dark:text-yellow-300 font-medium">
        Esta análise é informativa e <strong>NÃO substitui avaliação médica profissional</strong>.
        Consulte sempre um médico para diagnóstico e tratamento.
      </AlertDescription>
    </Alert>
  );
}
