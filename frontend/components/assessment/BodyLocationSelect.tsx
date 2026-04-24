'use client';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

const LOCATIONS = [
  'cabeça',
  'pescoço',
  'tórax',
  'abdômen',
  'costas',
  'membros superiores',
  'membros inferiores',
  'geral/sistêmico',
];

interface BodyLocationSelectProps {
  value: string;
  onChange: (value: string) => void;
}

export default function BodyLocationSelect({ value, onChange }: BodyLocationSelectProps) {
  return (
    <Select value={value} onValueChange={(v) => onChange(v ?? '')}>
      <SelectTrigger>
        <SelectValue placeholder="Localização no corpo" />
      </SelectTrigger>
      <SelectContent>
        {LOCATIONS.map((loc) => (
          <SelectItem key={loc} value={loc}>
            {loc.charAt(0).toUpperCase() + loc.slice(1)}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
