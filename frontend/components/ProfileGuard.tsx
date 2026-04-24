'use client';

import { useEffect } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import { useProfile } from '@/lib/api';

const PROFILE_PATH = '/profile';

export function ProfileGuard({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { data: profile, isLoading, isError } = useProfile();

  const onProfilePage = pathname === PROFILE_PATH;
  const needsSetup = !isLoading && !isError && !profile && !onProfilePage;

  useEffect(() => {
    if (needsSetup) {
      router.replace(PROFILE_PATH);
    }
  }, [needsSetup, router]);

  if (isLoading || needsSetup) {
    return (
      <div className="flex-1 flex items-center justify-center py-20">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return <>{children}</>;
}
