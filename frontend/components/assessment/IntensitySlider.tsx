'use client';

import { Slider } from '@/components/ui/slider';

interface IntensitySliderProps {
  value: number;
  onChange: (value: number) => void;
  label?: string;
}

function getColor(value: number) {
  if (value <= 3) return 'text-green-600 dark:text-green-400';
  if (value <= 6) return 'text-yellow-600 dark:text-yellow-400';
  return 'text-red-600 dark:text-red-400';
}

function getLabel(value: number) {
  if (value <= 3) return 'Leve';
  if (value <= 6) return 'Moderado';
  return 'Intenso';
}

export default function IntensitySlider({ value, onChange, label }: IntensitySliderProps) {
  return (
    <div className="space-y-2">
      {label && <p className="text-sm font-medium">{label}</p>}
      <div className="flex items-center gap-4">
        <Slider
          min={1}
          max={10}
          step={1}
          value={value}
          onValueChange={(v) => onChange(v as number)}
          className="flex-1"
        />
        <div className={`font-bold text-lg w-8 text-center ${getColor(value)}`}>
          {value}
        </div>
      </div>
      <div className="flex justify-between text-xs text-muted-foreground px-1">
        {Array.from({ length: 10 }, (_, i) => (
          <span key={i + 1}>{i + 1}</span>
        ))}
      </div>
      <p className={`text-xs font-medium ${getColor(value)}`}>{getLabel(value)}</p>
    </div>
  );
}
