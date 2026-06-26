import type { PropsWithChildren, ReactNode } from "react";

type SectionCardProps = PropsWithChildren<{
  title: string;
  subtitle?: string;
  aside?: ReactNode;
}>;

export function SectionCard({ title, subtitle, aside, children }: SectionCardProps) {
  return (
    <section className="panel">
      <header className="panel-header">
        <div>
          <h2>{title}</h2>
          {subtitle ? <p>{subtitle}</p> : null}
        </div>
        {aside}
      </header>
      {children}
    </section>
  );
}
