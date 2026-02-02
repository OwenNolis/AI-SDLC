export type ApiError = {
  correlationId: string;
  code: string;
  message: string;
  fieldErrors?: { field: string; message: string }[];
};

export async function postJson<TReq, TRes>(url: string, body: TReq): Promise<TRes> {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const text = await res.text();
  const json = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw json as ApiError;
  }
  return json as TRes;
}