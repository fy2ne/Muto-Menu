export default {
  async fetch(req, env) {
    const url = new URL(req.url);
    if (url.pathname === "/favicon.ico") {
      return env.ASSETS.fetch(new Request(new URL("/favicon-blue.png", req.url), req));
    }

    const res = await env.ASSETS.fetch(req);
    if (res.status === 404 && req.method === "GET") {
      const indexReq = new Request(new URL("/index.html", req.url), req);
      return env.ASSETS.fetch(indexReq);
    }
    return res;
  },
};
