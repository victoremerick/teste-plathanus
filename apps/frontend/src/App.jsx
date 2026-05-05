import { useState } from 'react'
import PedidoForm from './components/PedidoForm'
import PedidoList from './components/PedidoList'
import './App.css'

function App() {
  const [refresh, setRefresh] = useState(0)

  function handlePedidoCriado() {
    setRefresh((r) => r + 1)
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Central de Pedidos</h1>
        <p>Insira um pedido em linguagem natural e acompanhe os processados abaixo.</p>
      </header>

      <main className="app-main">
        <section className="app-section">
          <h2>Novo Pedido</h2>
          <PedidoForm onPedidoCriado={handlePedidoCriado} />
        </section>

        <section className="app-section">
          <h2>Pedidos</h2>
          <PedidoList refresh={refresh} />
        </section>
      </main>
    </div>
  )
}

export default App
