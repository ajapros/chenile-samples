import type { AuthResponse, DemoUser, IdentifyResponse, LoginResponse, ProviderOption, ServiceAResponse, ServiceMeResponse } from "../types";

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
const apiBaseUrl = configuredBaseUrl ? configuredBaseUrl.replace(/\/$/, "") : "/auth-api";
const configuredGatewayBaseUrl = import.meta.env.VITE_GATEWAY_BASE_URL?.trim();
const gatewayBaseUrl = configuredGatewayBaseUrl ? configuredGatewayBaseUrl.replace(/\/$/, "") : "/gateway-api";

async function request<T>(baseUrl: string, path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  if (!response.ok) {
    const payload = (await response.json().catch(() => ({}))) as { error?: string };
    throw new Error(payload.error ?? `Request failed with ${response.status}`);
  }

  return (await response.json()) as T;
}

export function identifyEmail(email: string): Promise<IdentifyResponse> {
  return request(apiBaseUrl, "/api/login/identify", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}

export function authenticateWithProvider(
  email: string,
  providerId: string,
  credential: string,
): Promise<LoginResponse> {
  return request(apiBaseUrl, "/api/login/authenticate", {
    method: "POST",
    body: JSON.stringify({ email, providerId, credential }),
  });
}

export function verifyMfa(challengeId: string, code: string): Promise<AuthResponse> {
  return request(apiBaseUrl, "/api/login/mfa/verify", {
    method: "POST",
    body: JSON.stringify({ challengeId, code }),
  });
}

export function fetchCurrentUser(accessToken: string): Promise<ServiceMeResponse> {
  return request(apiBaseUrl, "/api/service/me", {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}

export function fetchServiceAFlow(accessToken: string): Promise<ServiceAResponse> {
  return request(gatewayBaseUrl, "/api/a/orders/secure-bridge", {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}

export function fetchDemoUsers(): Promise<DemoUser[]> {
  return request(apiBaseUrl, "/api/login/demo-users");
}

export function startGoogleLogin(
  email: string,
  providerId: string,
): Promise<{ redirectUrl: string; provider: ProviderOption }> {
  return request(apiBaseUrl, "/api/login/google/start", {
    method: "POST",
    credentials: "include",
    body: JSON.stringify({ email, providerId }),
  });
}
