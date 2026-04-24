import { Stethoscope, FileText, Heart, AlertTriangle } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import type { SuggestionResponse, SugestaoTipoEnum } from '@/types/api';

const TIPO_CONFIG: Record<SugestaoTipoEnum, { label: string; Icon: React.ElementType; className: string }> = {
  especialista: { label: 'Especialista', Icon: Stethoscope, className: 'text-blue-600 dark:text-blue-400' },
  exame: { label: 'Exame', Icon: FileText, className: 'text-purple-600 dark:text-purple-400' },
  habito: { label: 'Hábito', Icon: Heart, className: 'text-green-600 dark:text-green-400' },
  urgencia: { label: 'Urgência', Icon: AlertTriangle, className: 'text-red-600 dark:text-red-400' },
};

interface SuggestionListProps {
  suggestions: SuggestionResponse[];
}

export default function SuggestionList({ suggestions }: SuggestionListProps) {
  if (!suggestions.length) return null;

  const grouped = suggestions.reduce<Record<SugestaoTipoEnum, SuggestionResponse[]>>(
    (acc, s) => {
      if (!acc[s.tipo]) acc[s.tipo] = [];
      acc[s.tipo].push(s);
      return acc;
    },
    {} as Record<SugestaoTipoEnum, SuggestionResponse[]>
  );

  const order: SugestaoTipoEnum[] = ['urgencia', 'especialista', 'exame', 'habito'];

  return (
    <div className="space-y-4">
      {order.filter((tipo) => grouped[tipo]?.length).map((tipo) => {
        const { label, Icon, className } = TIPO_CONFIG[tipo];
        const items = [...(grouped[tipo] || [])].sort((a, b) => a.prioridade - b.prioridade);

        return (
          <div key={tipo}>
            <div className={`flex items-center gap-2 mb-2 font-medium text-sm ${className}`}>
              <Icon className="h-4 w-4" />
              <span>{label}</span>
              <Badge variant="outline" className="text-xs">{items.length}</Badge>
            </div>
            <ul className="space-y-1 pl-6">
              {items.map((s) => (
                <li key={s.id} className="text-sm text-muted-foreground leading-relaxed">
                  {s.descricao}
                </li>
              ))}
            </ul>
          </div>
        );
      })}
    </div>
  );
}
