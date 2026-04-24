import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertTriangle } from 'lucide-react';

interface UrgencyBannerProps {
  urgente: boolean;
}

export default function UrgencyBanner({ urgente }: UrgencyBannerProps) {
  if (!urgente) return null;

  return (
    <Alert className="border-red-600 bg-red-50 dark:bg-red-950/40 dark:border-red-700">
      <AlertTriangle className="h-5 w-5 text-red-600 dark:text-red-400" />
      <AlertTitle className="text-red-800 dark:text-red-300 font-bold text-lg">
        ATENÇÃO: Possível Urgência Médica
      </AlertTitle>
      <AlertDescription className="text-red-700 dark:text-red-300 font-medium">
        Esta avaliação indica possível urgência médica.{' '}
        <strong>Busque atendimento médico imediatamente.</strong>
      </AlertDescription>
    </Alert>
  );
}
