export function decodeJwtPayload(token) {
  if (!token || typeof token !== "string") {
    return null;
  }
  const parts = token.split(".");
  if (parts.length !== 3) {
    return null;
  }
  try {
    const payload = parts[1];
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
    const json = atob(padded);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function getJwtRoles(token) {
  const payload = decodeJwtPayload(token);
  const roles = payload?.roles;
  if (Array.isArray(roles)) {
    return roles.filter((r) => typeof r === "string");
  }
  return [];
}

