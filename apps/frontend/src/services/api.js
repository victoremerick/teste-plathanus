const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export async function criarPedido(texto) {
  const response = await fetch(`${BASE_URL}/pedido`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ texto }),
  });

  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.message || `Erro ${response.status}: falha ao processar pedido.`);
  }

  return response.json();
}

export async function listarPedidos() {
  const response = await fetch(`${BASE_URL}/pedidos`);

  if (!response.ok) {
    throw new Error(`Erro ${response.status}: falha ao carregar pedidos.`);
  }

  return response.json();
}
