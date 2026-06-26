import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { KeyValueList } from "./components/KeyValueList";
import { SectionCard } from "./components/SectionCard";
import { ServiceGrid } from "./components/ServiceGrid";
import { TagList } from "./components/TagList";
import {
  authenticateWithProvider,
  fetchCurrentUser,
  fetchDemoUsers,
  fetchServiceAFlow,
  identifyEmail,
  startGoogleLogin,
} from "./lib/api";
import type { AuthResponse, DemoUser, IdentifyResponse, ServiceAResponse, ServiceMeResponse, StoredSession } from "./types";

const SESSION_STORAGE_KEY = "iam-demo-session";

type LoginStage = "identify" | "select-provider" | "authenticate";

export function App() {
  const [session, setSession] = useState<StoredSession | null>(() => readStoredSession());
  const [serviceData, setServiceData] = useState<ServiceMeResponse | null>(null);
  const [serviceAData, setServiceAData] = useState<ServiceAResponse | null>(null);
  const [demoUsers, setDemoUsers] = useState<DemoUser[]>([]);
  const [email, setEmail] = useState("");
  const [identifyResult, setIdentifyResult] = useState<IdentifyResponse | null>(null);
  const [selectedProviderId, setSelectedProviderId] = useState<number | null>(null);
  const [credential, setCredential] = useState("");
  const [loginStage, setLoginStage] = useState<LoginStage>("identify");
  const [loading, setLoading] = useState(false);
  const [bootLoading, setBootLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    fetchDemoUsers()
      .then((users) => {
        if (active) {
          setDemoUsers(users);
        }
      })
      .catch(() => {
        if (active) {
          setDemoUsers([]);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    const hash = new URLSearchParams(window.location.hash.replace(/^#/, ""));
    const accessToken = hash.get("access_token");
    const errorMessage = hash.get("error");

    if (errorMessage) {
      setError(decodeURIComponent(errorMessage));
      window.history.replaceState(null, "", window.location.pathname + window.location.search);
      setBootLoading(false);
      return;
    }

    if (!accessToken) {
      return;
    }

    const decodedToken = decodeURIComponent(accessToken);
    window.history.replaceState(null, "", window.location.pathname + window.location.search);
    setBootLoading(true);

    (async () => {
      try {
        const currentUser = await fetchCurrentUser(decodedToken);
        const serviceAResponse = await fetchServiceAFlow(decodedToken);
        const auth: AuthResponse = {
          generatedAt: currentUser.generatedAt,
          tokenType: "Bearer",
          expiresIn: 600,
          accessToken: decodedToken,
          tenant: {
            realm: currentUser.tenant.realm,
            displayName: currentUser.tenant.realm,
            issuer: currentUser.tenant.issuer,
          },
          user: currentUser.user,
          authentication: {
            clientId: currentUser.authentication.clientId,
            provider: {
              id: 0,
              providerKey: currentUser.authentication.providerKey,
              providerLabel: currentUser.authentication.providerKey,
              providerType: currentUser.authentication.providerType,
              realm: currentUser.tenant.realm,
              realmDisplayName: currentUser.tenant.realm,
              username: currentUser.user.username,
              email: currentUser.user.email,
            },
            scopes: currentUser.access.scopes,
            roles: currentUser.access.roles,
            acls: currentUser.access.acls,
          },
        };
        const nextSession: StoredSession = { accessToken: decodedToken, auth };
        window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(nextSession));
        setSession(nextSession);
        setServiceData(currentUser);
        setServiceAData(serviceAResponse);
      } catch (callbackError) {
        setError(callbackError instanceof Error ? callbackError.message : "Google login callback failed");
      } finally {
        setBootLoading(false);
      }
    })();
  }, []);

  useEffect(() => {
    let active = true;

    async function boot() {
      if (!session) {
        setBootLoading(false);
        return;
      }

      try {
        const response = await fetchCurrentUser(session.accessToken);
        const serviceAResponse = await fetchServiceAFlow(session.accessToken);
        if (!active) {
          return;
        }
        setServiceData(response);
        setServiceAData(serviceAResponse);
      } catch {
        if (!active) {
          return;
        }
        clearSessionState();
      } finally {
        if (active) {
          setBootLoading(false);
        }
      }
    }

    void boot();
    return () => {
      active = false;
    };
  }, [session]);

  function clearSessionState() {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    setSession(null);
    setServiceData(null);
    setServiceAData(null);
    setIdentifyResult(null);
    setSelectedProviderId(null);
    setCredential("");
    setLoginStage("identify");
    setEmail("");
  }

  async function handleIdentifySubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const response = await identifyEmail(email);
      setIdentifyResult(response);
      setSelectedProviderId(response.autoSelectedProviderId);
      setCredential("");
      setLoginStage(response.nextStep === "authenticate" ? "authenticate" : "select-provider");
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Unable to resolve authentication flow");
    } finally {
      setLoading(false);
    }
  }

  async function handleProviderContinue(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedProviderId) {
      setError("Select an authentication provider");
      return;
    }
    setError("");
    setCredential("");
    setLoginStage("authenticate");
  }

  async function handleAuthenticateSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!identifyResult || !selectedProviderId) {
      setError("Authentication context is missing");
      return;
    }

    setLoading(true);
    setError("");

    try {
      if (selectedProvider?.providerType === "GOOGLE") {
        const response = await startGoogleLogin(identifyResult.email, selectedProviderId);
        const redirectUrl = response.redirectUrl.startsWith("http")
          ? response.redirectUrl
          : `${import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:9000"}${response.redirectUrl}`;
        window.location.assign(redirectUrl);
        return;
      }
      const auth = await authenticateWithProvider(identifyResult.email, selectedProviderId, credential);
      const nextSession: StoredSession = {
        accessToken: auth.accessToken,
        auth,
      };
      window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(nextSession));
      setSession(nextSession);
      const currentUser = await fetchCurrentUser(nextSession.accessToken);
      const serviceAResponse = await fetchServiceAFlow(nextSession.accessToken);
      setServiceData(currentUser);
      setServiceAData(serviceAResponse);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Authentication failed");
    } finally {
      setLoading(false);
    }
  }

  const selectedProvider =
    identifyResult?.providers.find((provider) => provider.id === selectedProviderId) ?? null;
  const credentialLabel =
    selectedProvider?.providerType === "OTP"
      ? "One-time passcode"
      : selectedProvider?.providerType === "GOOGLE"
        ? "Google account"
        : "Password";
  const credentialHint =
    selectedProvider?.providerType === "OTP"
      ? identifyResult?.credentialHints.otp
      : selectedProvider?.providerType === "GOOGLE"
        ? identifyResult?.credentialHints.google
        : identifyResult?.credentialHints.password;

  if (bootLoading) {
    return <main className="app-shell"><section className="panel status">Loading session...</section></main>;
  }

  if (!session || !serviceData || !serviceAData) {
    return (
      <main className="app-shell app-shell--login">
        <section className="hero">
          <div className="hero-copy">
            <p className="eyebrow">Independent React IAM Demo</p>
            <h1>Email-first login with tenant and provider discovery.</h1>
            <p className="lede">
              If the user is not authenticated, the app lands on login. After the email step, the backend resolves
              the tenant and active providers, initiates a direct flow for single-provider users, or asks the user to
              choose a provider when multiple auth methods are available.
            </p>
          </div>
          <div className="hero-panel">
            <span>Flow</span>
            <strong>Email - provider resolution - login - service API landing page</strong>
            <span>Backend</span>
            <strong>`/api/login/*` + `/api/service/me`</strong>
          </div>
        </section>

        <section className="login-layout">
          <SectionCard
            title="Sign in"
            subtitle={
              loginStage === "identify"
                ? "Start with the user email address."
                : identifyResult
                  ? `${identifyResult.tenant.displayName} resolved for ${identifyResult.email}`
                  : "Continue the resolved authentication flow."
            }
          >
            {loginStage === "identify" ? (
              <form className="form-stack" onSubmit={handleIdentifySubmit}>
                <label className="field">
                  <span>Email address</span>
                  <input
                    autoComplete="email"
                    name="email"
                    placeholder="gaurav.bhardwaj@getvymo.com"
                    type="email"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                  />
                </label>
                <button className="primary-button" disabled={loading} type="submit">
                  {loading ? "Resolving..." : "Continue"}
                </button>
              </form>
            ) : null}

            {loginStage === "select-provider" && identifyResult ? (
              <form className="form-stack" onSubmit={handleProviderContinue}>
                <div className="notice">
                  Multiple providers are available for this account. Choose which authentication flow to follow.
                </div>
                <label className="field">
                  <span>Authentication provider</span>
                  <select
                    value={selectedProviderId ?? ""}
                    onChange={(event) => setSelectedProviderId(Number(event.target.value))}
                  >
                    <option value="" disabled>
                      Select a provider
                    </option>
                    {identifyResult.providers.map((provider) => (
                      <option key={provider.id} value={provider.id}>
                        {provider.providerLabel} ({provider.providerType})
                      </option>
                    ))}
                  </select>
                </label>
                <div className="action-row">
                  <button className="secondary-button" type="button" onClick={() => resetLogin()}>
                    Back
                  </button>
                  <button className="primary-button" type="submit">
                    Continue
                  </button>
                </div>
              </form>
            ) : null}

            {loginStage === "authenticate" && identifyResult && selectedProvider ? (
              <form className="form-stack" onSubmit={handleAuthenticateSubmit}>
                <div className="provider-summary">
                  <strong>{selectedProvider.providerLabel}</strong>
                  <span>{selectedProvider.providerType} for {selectedProvider.realmDisplayName}</span>
                </div>
                {selectedProvider.providerType === "GOOGLE" ? (
                  <div className="notice notice--google">
                    Continue with Google for this account. You will be redirected to Google, and after successful
                    consent the browser returns here with the issued platform token.
                  </div>
                ) : (
                  <label className="field">
                    <span>{credentialLabel}</span>
                    <input
                      autoComplete={selectedProvider.providerType === "OTP" ? "one-time-code" : "current-password"}
                      placeholder={selectedProvider.providerType === "OTP" ? "Enter OTP code" : "Enter password"}
                      type={selectedProvider.providerType === "OTP" ? "text" : "password"}
                      value={credential}
                      onChange={(event) => setCredential(event.target.value)}
                    />
                  </label>
                )}
                <div className="credential-hint">
                  Demo hint: <code>{credentialHint}</code>
                </div>
                <div className="action-row">
                  <button className="secondary-button" type="button" onClick={() => setLoginStage(identifyResult.providers.length > 1 ? "select-provider" : "identify")}>
                    Back
                  </button>
                  <button className="primary-button" disabled={loading} type="submit">
                    {loading ? "Signing in..." : selectedProvider.providerType === "GOOGLE" ? "Continue with Google" : "Sign in"}
                  </button>
                </div>
              </form>
            ) : null}

            {error ? <div className="inline-error">{error}</div> : null}
          </SectionCard>

          <SectionCard
            title="Demo accounts"
            subtitle="Use these seeded users to exercise password and OTP flows."
          >
            <div className="demo-user-list">
              {demoUsers.map((user) => (
                <button
                  className="demo-user-card"
                  key={user.email}
                  type="button"
                  onClick={() => {
                    setEmail(user.email);
                    setError("");
                    setLoginStage("identify");
                  }}
                >
                  <strong>{user.email}</strong>
                  <span>Password: {user.password}</span>
                  <span>OTP: {user.otp}</span>
                  <span>Google: {user.google}</span>
                </button>
              ))}
            </div>
          </SectionCard>
        </section>
      </main>
    );
  }

  const auth: AuthResponse = session.auth;
  const claims = serviceData.tokenClaims;

  return (
    <main className="app-shell">
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">Authenticated Landing Page</p>
          <h1>Service API response after successful login.</h1>
          <p className="lede">
            The user is authenticated. This landing page calls the protected user-context API and the real
            `gateway - service-a - service-b` path, then renders the end-to-end response.
          </p>
        </div>
        <div className="hero-panel">
          <span>Signed in as</span>
          <strong>{serviceData.user.email}</strong>
          <span>Tenant</span>
          <strong>{auth.tenant.displayName}</strong>
          <button className="secondary-button secondary-button--hero" onClick={clearSessionState} type="button">
            Log out
          </button>
        </div>
      </section>

      <section className="content-grid">
        <SectionCard title="User profile">
          <KeyValueList
            items={[
              { label: "Tenant", value: `${auth.tenant.displayName} (${serviceData.tenant.realm})` },
              { label: "User ID", value: serviceData.user.id },
              { label: "Username", value: serviceData.user.username },
              { label: "Email", value: serviceData.user.email },
              { label: "Issuer", value: serviceData.tenant.issuer },
            ]}
          />
        </SectionCard>

        <SectionCard title="Authentication context">
          <KeyValueList
            items={[
              { label: "Client ID", value: serviceData.authentication.clientId },
              { label: "Provider key", value: serviceData.authentication.providerKey },
              { label: "Provider type", value: serviceData.authentication.providerType },
              { label: "Login timestamp", value: auth.generatedAt },
            ]}
          />
          <TagList title="Scopes" values={serviceData.access.scopes} />
          <TagList title="Roles" values={serviceData.access.roles} />
          <TagList title="ACLs" values={serviceData.access.acls} />
        </SectionCard>

        <SectionCard title="Service access" subtitle="Granted services are derived from the access token scopes.">
          <ServiceGrid services={serviceData.access.services} />
        </SectionCard>

        <SectionCard title="Token audiences">
          <TagList title="Audience" values={serviceData.access.audiences} />
        </SectionCard>
      </section>

      <section className="content-grid content-grid--wide">
        <SectionCard title="Protected service response" subtitle={`Generated at ${serviceData.generatedAt}`}>
          <pre>{JSON.stringify(serviceData, null, 2)}</pre>
        </SectionCard>

        <SectionCard title="Access token claims">
          <pre>{JSON.stringify(claims, null, 2)}</pre>
        </SectionCard>
      </section>

      <section className="content-grid content-grid--wide">
        <SectionCard
          title="End-to-End Service Flow"
          subtitle="Browser -> Gateway -> Service A -> Service B with tenant and ACL relay"
        >
          <pre>{JSON.stringify(serviceAData, null, 2)}</pre>
        </SectionCard>

        <SectionCard title="Flow highlights">
          <KeyValueList
            items={[
              { label: "Gateway path", value: "/api/a/orders/secure-bridge" },
              { label: "Service", value: String(serviceAData.service ?? "") },
              { label: "Tenant relay", value: String(serviceAData.requestContext?.tenantId ?? "") },
              { label: "User relay", value: String(serviceAData.requestContext?.userId ?? "") },
              {
                label: "Downstream tenant",
                value: String(
                  (serviceAData.downstreamContext?.requestContext as Record<string, unknown> | undefined)?.tenantId ?? "",
                ),
              },
            ]}
          />
        </SectionCard>
      </section>
    </main>
  );

  function resetLogin() {
    setIdentifyResult(null);
    setSelectedProviderId(null);
    setCredential("");
    setLoginStage("identify");
    setError("");
  }
}

function readStoredSession(): StoredSession | null {
  const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as StoredSession;
  } catch {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return null;
  }
}
