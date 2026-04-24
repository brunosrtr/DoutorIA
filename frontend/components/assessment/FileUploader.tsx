'use client';

import { useCallback, useRef, useState } from 'react';
import { Upload, X, FileText, ImageIcon, AlertCircle } from 'lucide-react';
import { Badge } from '@/components/ui/badge';

const MAX_FILES = 5;
const MAX_SIZE = 10 * 1024 * 1024; // 10 MB
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'application/pdf'];

interface FileItem {
  file: File;
  id: string;
  error?: string;
}

interface FileUploaderProps {
  files: FileItem[];
  onChange: (files: FileItem[]) => void;
  disabled?: boolean;
}

function FileIcon({ type }: { type: string }) {
  if (type === 'application/pdf') return <FileText className="h-5 w-5 text-red-500" />;
  return <ImageIcon className="h-5 w-5 text-blue-500" />;
}

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function validate(file: File): string | undefined {
  if (!ALLOWED_TYPES.includes(file.type)) return 'Tipo não suportado (use JPG, PNG ou PDF)';
  if (file.size > MAX_SIZE) return 'Arquivo maior que 10 MB';
}

export default function FileUploader({ files, onChange, disabled }: FileUploaderProps) {
  const [dragging, setDragging] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const addFiles = useCallback(
    (incoming: File[]) => {
      const remaining = MAX_FILES - files.length;
      if (remaining <= 0) return;

      const newItems: FileItem[] = incoming.slice(0, remaining).map((f) => ({
        file: f,
        id: `${f.name}-${f.size}-${Date.now()}-${Math.random()}`,
        error: validate(f),
      }));

      onChange([...files, ...newItems]);
    },
    [files, onChange],
  );

  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    setDragging(false);
    if (disabled) return;
    addFiles(Array.from(e.dataTransfer.files));
  }

  function handleInputChange(e: React.ChangeEvent<HTMLInputElement>) {
    if (e.target.files) {
      addFiles(Array.from(e.target.files));
      e.target.value = '';
    }
  }

  function removeFile(id: string) {
    onChange(files.filter((f) => f.id !== id));
  }

  const canAdd = files.length < MAX_FILES && !disabled;

  return (
    <div className="space-y-3">
      {/* Drop zone */}
      <div
        role="button"
        tabIndex={canAdd ? 0 : -1}
        aria-label="Área de upload de arquivos"
        onClick={() => canAdd && inputRef.current?.click()}
        onKeyDown={(e) => e.key === 'Enter' && canAdd && inputRef.current?.click()}
        onDragOver={(e) => { e.preventDefault(); if (canAdd) setDragging(true); }}
        onDragLeave={() => setDragging(false)}
        onDrop={handleDrop}
        className={[
          'border-2 border-dashed rounded-lg p-6 text-center transition-colors',
          canAdd ? 'cursor-pointer' : 'cursor-not-allowed opacity-50',
          dragging ? 'border-primary bg-primary/5' : 'border-muted-foreground/30 hover:border-primary/50',
        ].join(' ')}
      >
        <Upload className="h-8 w-8 mx-auto mb-2 text-muted-foreground" />
        <p className="text-sm font-medium">
          {canAdd ? 'Arraste arquivos ou clique para selecionar' : 'Limite de arquivos atingido'}
        </p>
        <p className="text-xs text-muted-foreground mt-1">
          JPG, PNG ou PDF · máx. 10 MB por arquivo · {files.length}/{MAX_FILES}
        </p>
        <input
          ref={inputRef}
          type="file"
          multiple
          accept=".jpg,.jpeg,.png,.pdf"
          className="hidden"
          onChange={handleInputChange}
          disabled={!canAdd}
        />
      </div>

      {/* File list */}
      {files.length > 0 && (
        <ul className="space-y-2">
          {files.map((item) => (
            <li
              key={item.id}
              className={[
                'flex items-center gap-3 p-3 border rounded-lg text-sm',
                item.error ? 'border-destructive/50 bg-destructive/5' : 'border-border',
              ].join(' ')}
            >
              {item.error ? (
                <AlertCircle className="h-5 w-5 text-destructive shrink-0" />
              ) : (
                <FileIcon type={item.file.type} />
              )}
              <div className="flex-1 min-w-0">
                <p className="font-medium truncate">{item.file.name}</p>
                <p className="text-xs text-muted-foreground">{formatBytes(item.file.size)}</p>
                {item.error && (
                  <p className="text-xs text-destructive mt-0.5">{item.error}</p>
                )}
              </div>
              <Badge variant={item.error ? 'destructive' : 'secondary'} className="shrink-0 text-xs">
                {item.error ? 'Inválido' : item.file.type === 'application/pdf' ? 'PDF' : 'Imagem'}
              </Badge>
              {!disabled && (
                <button
                  onClick={(e) => { e.stopPropagation(); removeFile(item.id); }}
                  aria-label="Remover arquivo"
                  className="shrink-0 text-muted-foreground hover:text-destructive transition-colors"
                >
                  <X className="h-4 w-4" />
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
