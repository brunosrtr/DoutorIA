'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';
import { useProfile, useSaveProfile } from '@/lib/api';
import { Loader2, UserCircle2 } from 'lucide-react';

const profileSchema = z.object({
  nome: z.string().min(2, 'Nome muito curto'),
  dataNascimento: z.string().optional(),
  sexo: z.enum(['masculino', 'feminino', 'outro', 'nao_informado']).optional(),
  tipoSanguineo: z.string().optional(),
});

type ProfileForm = z.infer<typeof profileSchema>;

export default function ProfilePage() {
  const router = useRouter();
  const { data: profile, isLoading } = useProfile();
  const saveProfile = useSaveProfile();

  const form = useForm<ProfileForm>({
    resolver: zodResolver(profileSchema),
    defaultValues: { nome: '' },
  });

  useEffect(() => {
    if (profile) {
      form.reset({
        nome: profile.nome,
        dataNascimento: profile.dataNascimento ?? undefined,
        sexo: profile.sexo,
        tipoSanguineo: profile.tipoSanguineo ?? undefined,
      });
    }
  }, [profile, form]);

  async function onSubmit(data: ProfileForm) {
    try {
      await saveProfile.mutateAsync(data);
      router.push('/');
    } catch (e: unknown) {
      form.setError('root', { message: (e as Error).message });
    }
  }

  if (isLoading) {
    return (
      <div className="max-w-xl mx-auto py-8 px-4 space-y-4">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  const isFirstVisit = !profile;

  return (
    <div className="max-w-xl mx-auto py-8 px-4 space-y-6">
      <div className="space-y-2">
        <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-primary/10 text-primary text-xs font-medium">
          <UserCircle2 className="h-3 w-3" /> {isFirstVisit ? 'Configuração inicial' : 'Meu perfil'}
        </div>
        <h1 className="text-2xl font-bold tracking-tight">
          {isFirstVisit ? 'Vamos começar' : 'Meu perfil'}
        </h1>
        <p className="text-muted-foreground text-sm">
          {isFirstVisit
            ? 'Preencha alguns dados básicos para que a IA possa analisar seus sintomas com mais contexto.'
            : 'Mantenha seus dados atualizados para análises mais precisas.'}
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Dados pessoais</CardTitle>
          <CardDescription>
            Usados apenas para personalizar suas análises — você pode alterar a qualquer momento.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label>Nome *</Label>
              <Input {...form.register('nome')} placeholder="Como podemos te chamar?" />
              {form.formState.errors.nome && (
                <p className="text-xs text-destructive">{form.formState.errors.nome.message}</p>
              )}
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Data de nascimento</Label>
                <Input type="date" {...form.register('dataNascimento')} />
              </div>
              <div className="space-y-2">
                <Label>Tipo sanguíneo</Label>
                <Select
                  value={form.watch('tipoSanguineo') ?? ''}
                  onValueChange={(v) => form.setValue('tipoSanguineo', v || undefined)}
                >
                  <SelectTrigger><SelectValue placeholder="Tipo" /></SelectTrigger>
                  <SelectContent>
                    {['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'].map((t) => (
                      <SelectItem key={t} value={t}>{t}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className="space-y-2">
              <Label>Sexo</Label>
              <Select
                value={form.watch('sexo') ?? ''}
                onValueChange={(v) => form.setValue('sexo', v as ProfileForm['sexo'])}
              >
                <SelectTrigger><SelectValue placeholder="Sexo" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="masculino">Masculino</SelectItem>
                  <SelectItem value="feminino">Feminino</SelectItem>
                  <SelectItem value="outro">Outro</SelectItem>
                  <SelectItem value="nao_informado">Prefiro não informar</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {form.formState.errors.root && (
              <p className="text-xs text-destructive">{form.formState.errors.root.message}</p>
            )}

            <Button type="submit" disabled={saveProfile.isPending} className="w-full">
              {saveProfile.isPending && <Loader2 className="h-4 w-4 animate-spin mr-2" />}
              {isFirstVisit ? 'Começar a usar' : 'Salvar alterações'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
