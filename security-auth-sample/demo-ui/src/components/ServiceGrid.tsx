type ServiceGridProps = {
  services: Array<{
    service: string;
    scope: string;
    audience: string;
    granted: boolean;
  }>;
};

export function ServiceGrid({ services }: ServiceGridProps) {
  return (
    <div className="service-grid">
      {services.map((service) => (
        <article
          className={`service-tile ${service.granted ? "service-tile--granted" : "service-tile--blocked"}`}
          key={service.service}
        >
          <h3>{service.service}</h3>
          <p>{service.scope}</p>
          <small>audience: {service.audience}</small>
          <strong>{service.granted ? "Granted" : "Not granted"}</strong>
        </article>
      ))}
    </div>
  );
}
