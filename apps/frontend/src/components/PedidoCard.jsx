function formatDate(dateStr) {
  if (!dateStr) return '—';
  const date = new Date(dateStr);
  return date.toLocaleDateString('pt-BR');
}

function formatDateTime(dateStr) {
  if (!dateStr) return '—';
  const date = new Date(dateStr);
  return date.toLocaleString('pt-BR');
}

export default function PedidoCard({ pedido }) {
  return (
    <article className="pedido-card">
      <header className="card-header">
        <span className="card-id">#{pedido.id}</span>
        <span className="card-cliente">{pedido.cliente || '—'}</span>
      </header>

      <p className="card-texto">{pedido.textoOriginal}</p>

      <dl className="card-meta">
        <div className="card-meta-item">
          <dt>Data de entrega</dt>
          <dd>{formatDate(pedido.dataEntrega)}</dd>
        </div>
        <div className="card-meta-item">
          <dt>Criado em</dt>
          <dd>{formatDateTime(pedido.createdAt)}</dd>
        </div>
      </dl>

      {pedido.itens && pedido.itens.length > 0 && (
        <ul className="card-itens">
          {pedido.itens.map((item, i) => (
            <li key={`${item.produto}-${item.quantidade}-${item.unidade}-${i}`} className="card-item">
              <span className="item-qty">{item.quantidade}</span>
              <span className="item-unit">{item.unidade}</span>
              <span className="item-produto">{item.produto}</span>
            </li>
          ))}
        </ul>
      )}
    </article>
  );
}
