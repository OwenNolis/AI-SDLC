import { HttpError, postJson } from "./http";
import type { ApiError } from "./http";

// ── HttpError ──────────────────────────────────────────────

describe("HttpError", () => {
  const apiError: ApiError = {
    correlationId: "abc-123",
    code: "VALIDATION_ERROR",
    message: "Something went wrong",
    fieldErrors: [{ field: "email", message: "invalid" }],
  };

  it("uses apiError.message when no custom message is given", () => {
    const err = new HttpError(apiError);
    expect(err.message).toBe("Something went wrong");
    expect(err.name).toBe("HttpError");
  });

  it("uses the custom message when provided", () => {
    const err = new HttpError(apiError, "custom msg");
    expect(err.message).toBe("custom msg");
  });

  it("copies correlationId, code and fieldErrors from the ApiError", () => {
    const err = new HttpError(apiError);
    expect(err.correlationId).toBe("abc-123");
    expect(err.code).toBe("VALIDATION_ERROR");
    expect(err.fieldErrors).toEqual([{ field: "email", message: "invalid" }]);
  });

  it("is instanceof Error and HttpError", () => {
    const err = new HttpError(apiError);
    expect(err).toBeInstanceOf(Error);
    expect(err).toBeInstanceOf(HttpError);
  });
});

// ── postJson ───────────────────────────────────────────────

describe("postJson", () => {
  const originalFetch = globalThis.fetch;

  afterEach(() => {
    globalThis.fetch = originalFetch;
  });

  it("returns parsed JSON on a successful response", async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      ok: true,
      text: () => Promise.resolve(JSON.stringify({ id: 1 })),
    });

    const result = await postJson<{ name: string }, { id: number }>(
      "/api/test",
      { name: "test" },
    );

    expect(result).toEqual({ id: 1 });
    expect(fetch).toHaveBeenCalledWith("/api/test", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: "test" }),
    });
  });

  it("throws HttpError when the response has a JSON error body", async () => {
    const errorBody: ApiError = {
      correlationId: "err-1",
      code: "BAD_REQUEST",
      message: "bad request",
    };

    globalThis.fetch = jest.fn().mockResolvedValue({
      ok: false,
      status: 400,
      text: () => Promise.resolve(JSON.stringify(errorBody)),
    });

    await expect(postJson("/api/fail", {})).rejects.toThrow(HttpError);
    await expect(postJson("/api/fail", {})).rejects.toMatchObject({
      correlationId: "err-1",
      code: "BAD_REQUEST",
    });
  });

  it("throws a generic Error when the error response body is empty", async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      ok: false,
      status: 502,
      statusText: "Bad Gateway",
      text: () => Promise.resolve(""),
    });

    await expect(postJson("/api/empty", {})).rejects.toThrow(
      "HTTP error! status: 502 - Bad Gateway",
    );
  });

  it("throws a generic Error with fallback text when statusText is empty", async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      ok: false,
      status: 500,
      statusText: "",
      text: () => Promise.resolve(""),
    });

    await expect(postJson("/api/no-text", {})).rejects.toThrow(
      "HTTP error! status: 500 - Unknown error",
    );
  });
});
