export type ApiError = {
  correlationId: string;
  code: string;
  message: string;
  fieldErrors?: { field: string; message: string }[];
};

// Custom error class to satisfy SonarQube S3696
export class HttpError extends Error {
  public readonly details: ApiError;

  constructor(message: string, details: ApiError) {
    super(message);
    this.name = "HttpError";
    this.details = details;
    // Set the prototype explicitly to ensure proper inheritance for custom errors
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
    const apiError: ApiError = json || {
      correlationId: "unknown",
      code: "HTTP_ERROR",
      message: `Request failed with status ${res.status}`,
    };
    throw new HttpError(apiError.message, apiError);
  }
  return json as TRes;
}
