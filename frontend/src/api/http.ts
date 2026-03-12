export type ApiError = {
  correlationId: string;
  code: string;
  message: string;
  fieldErrors?: { field: string; message: string }[];
};

// SonarQube: Expected an error object to be thrown. (typescript:S3696)
export class HttpError extends Error {
  public readonly correlationId: string;
  public readonly code: string;
  public readonly fieldErrors?: { field: string; message: string }[];

  constructor(apiError: ApiError, message?: string) {
    super(message || apiError.message);
    this.name = "HttpError";
    this.correlationId = apiError.correlationId;
    this.code = apiError.code;
    this.fieldErrors = apiError.fieldErrors;

    // Restore prototype chain for correct instanceof checks
    Object.setPrototypeOf(this, HttpError.prototype);
  }
}

export async function postJson<TReq, TRes>(url: string, body: TReq): Promise<TRes> {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const text = await res.text();
  const json = text ? JSON.parse(text) : null;

  if (!res.ok) {
    if (json) {
      throw new HttpError(json as ApiError);
    } else {
      // Fallback for cases where response text is empty but status is not ok
      throw new Error(`HTTP error! status: ${res.status} - ${res.statusText || 'Unknown error'}`);
    }
  }
  return json as TRes;
}
