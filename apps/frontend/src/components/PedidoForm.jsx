import { useState } from 'react';
import { criarPedido } from '../services/api';

export default function PedidoForm({ onPedidoCriado }) {
  const [texto, setTexto] = useState('');
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    if (!texto.trim()) return;

    setLoading(true);
    setErro('');

    try {
      const pedido = await criarPedido(texto.trim());
      setTexto('');
      onPedidoCriado(pedido);
    } catch (err) {
      setErro(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="pedido-form" onSubmit={handleSubmit}>
      <label htmlFor="pedido-texto" className="form-label">
        Inserir pedido
      </label>
      <textarea
        id="pedido-texto"
        className="form-textarea"
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
        placeholder="Ex: João quer 2 caixas de leite e 3 pacotes de arroz para amanhã."
        rows={5}
        disabled={loading}
      />
      {erro && <p className="form-error">{erro}</p>}
      <button type="submit" className="form-btn" disabled={loading || !texto.trim()}>
        {loading ? 'Processando…' : 'Processar pedido'}
      </button>
    </form>
  );
}
