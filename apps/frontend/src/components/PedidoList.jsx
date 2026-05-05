import { useState, useEffect, useCallback } from 'react';
import { listarPedidos } from '../services/api';
import PedidoCard from './PedidoCard';

export default function PedidoList({ refresh }) {
  const [pedidos, setPedidos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState('');
  const [retryCount, setRetryCount] = useState(0);

  const retry = useCallback(() => setRetryCount((c) => c + 1), []);

  useEffect(() => {
    let cancelled = false;

    async function fetchPedidos() {
      setErro('');
      setLoading(true);
      try {
        const data = await listarPedidos();
        if (!cancelled) {
          setPedidos(data);
        }
      } catch (err) {
        if (!cancelled) {
          setErro(err.message);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    fetchPedidos();

    return () => {
      cancelled = true;
    };
  }, [refresh, retryCount]);

  if (loading) {
    return <p className="list-status">Carregando pedidos…</p>;
  }

  if (erro) {
    return (
      <div className="list-error">
        <p>{erro}</p>
        <button className="form-btn" onClick={retry}>
          Tentar novamente
        </button>
      </div>
    );
  }

  if (pedidos.length === 0) {
    return <p className="list-status">Nenhum pedido encontrado.</p>;
  }

  return (
    <section className="pedido-list">
      {pedidos.map((pedido) => (
        <PedidoCard key={pedido.id} pedido={pedido} />
      ))}
    </section>
  );
}
