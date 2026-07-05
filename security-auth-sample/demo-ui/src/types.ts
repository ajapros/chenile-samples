export type ProviderOption = {
  id: string;
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
  autoSelectedProviderId: string | null;
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
    id: string;
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

export type MfaChallengeResponse = {
  generatedAt: string;
  nextStep: "mfa";
  challengeId: string;
  expiresAt: string;
  provider: {
    providerKey: string;
    providerType: string;
    providerLabel: string;
    destinationHint: string;
  };
  tenant: {
    realm: string;
    displayName: string;
    issuer: string;
  };
  user: {
    id: string;
    username: string;
    email: string;
  };
  authentication: {
    clientId: string;
    provider: ProviderOption;
  };
};

export type LoginResponse = AuthResponse | MfaChallengeResponse;

export type ServiceMeResponse = {
  generatedAt: string;
  tenant: {
    realm: string;
    issuer: string;
  };
  user: {
    id: string;
    username: string;
    email: string;
  };
  authentication: {
    providerKey: string;
    providerType: string;
    clientId: string;
    mfa: boolean;
    amr: string[];
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
