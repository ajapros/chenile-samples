export type ProviderOption = {
  id: number;
  providerKey: string;
  providerLabel: string;
  providerType: string;
  realm: string;
  realmDisplayName: string;
  username: string;
  email: string;
};

export type IdentifyResponse = {
  email: string;
  tenant: {
    realm: string;
    displayName: string;
  };
  providers: ProviderOption[];
  nextStep: "authenticate" | "select-provider";
  autoSelectedProviderId: number | null;
  credentialHints: {
    email: string;
    password: string;
    otp: string;
    google: string;
  };
};

export type AuthResponse = {
  generatedAt: string;
  tokenType: string;
  expiresIn: number;
  accessToken: string;
  tenant: {
    realm: string;
    displayName: string;
    issuer: string;
  };
  user: {
    id: number;
    username: string;
    email: string;
  };
  authentication: {
    clientId: string;
    provider: ProviderOption;
    scopes: string[];
    roles: string[];
    acls: string[];
  };
};

export type ServiceMeResponse = {
  generatedAt: string;
  tenant: {
    realm: string;
    issuer: string;
  };
  user: {
    id: number;
    username: string;
    email: string;
  };
  authentication: {
    providerKey: string;
    providerType: string;
    clientId: string;
  };
  access: {
    scopes: string[];
    roles: string[];
    acls: string[];
    audiences: string[];
    services: Array<{
      service: string;
      scope: string;
      audience: string;
      granted: boolean;
    }>;
  };
  tokenClaims: Record<string, unknown>;
};

export type ServiceAResponse = {
  service: string;
  requestContext: Record<string, unknown>;
  orders: Array<Record<string, unknown>>;
  downstreamContext: Record<string, unknown>;
  downstreamPortfolio: Record<string, unknown>;
};

export type DemoUser = {
  email: string;
  password: string;
  otp: string;
  google: string;
};

export type StoredSession = {
  accessToken: string;
  auth: AuthResponse;
};
