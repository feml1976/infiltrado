import { useState, type FormEvent } from 'react'
import axios from 'axios'
import { useLobby } from './hooks/useLobby'
import { getPlayerColor } from '@/shared/utils/playerColors'

export function LobbyPage() {
  const { codigoSala, estadoQuery, crearMutation, unirMutation, irAPartida, salirDeSala } =
    useLobby()

  const [codigoInput,    setCodigoInput]    = useState('')
  const [numRondas,      setNumRondas]      = useState(3)
  const [numJugadores,   setNumJugadores]   = useState(4)
  const [numInfiltrados, setNumInfiltrados] = useState(1)

  const unirError = unirMutation.error
    ? (axios.isAxiosError(unirMutation.error)
        ? (unirMutation.error.response?.data as { mensaje?: string })?.mensaje
        : undefined) ?? 'No se pudo unir a la sala'
    : null

  const crearError = crearMutation.error
    ? (axios.isAxiosError(crearMutation.error)
        ? (crearMutation.error.response?.data as { mensaje?: string })?.mensaje
        : undefined) ?? 'No se pudo crear la sala'
    : null

  function handleUnirse(e: FormEvent) {
    e.preventDefault()
    if (codigoInput.trim()) unirMutation.mutate(codigoInput.trim())
  }

  function handleCrear(e: FormEvent) {
    e.preventDefault()
    crearMutation.mutate({ numRondas, numInfiltrados, numJugadores })
  }

  const partida = estadoQuery.data

  return (
    <div className="page-top">
      <div className="card card-wide">
        {!codigoSala ? (
          <>
            <h2>Unirse a sala</h2>
            {unirError && <div className="alert-error">{unirError}</div>}
            <form onSubmit={handleUnirse}>
              <div className="field">
                <label htmlFor="codigo-sala">Código de sala (6 caracteres)</label>
                <input
                  id="codigo-sala"
                  type="text"
                  value={codigoInput}
                  onChange={(e) => setCodigoInput(e.target.value.toUpperCase())}
                  placeholder="XXXXXX"
                  maxLength={6}
                  required
                />
              </div>
              <button
                type="submit"
                className="btn btn-primary btn-block"
                disabled={unirMutation.isPending}
              >
                {unirMutation.isPending ? 'Uniéndose…' : 'Unirse'}
              </button>
            </form>

            <hr className="divider" />

            <h2>Crear nueva sala</h2>
            {crearError && <div className="alert-error">{crearError}</div>}
            <form onSubmit={handleCrear}>
              <div className="flex-gap-2">
                <div className="field" style={{ flex: 1 }}>
                  <label htmlFor="num-rondas">Rondas</label>
                  <input
                    id="num-rondas"
                    type="number"
                    min={1}
                    max={10}
                    value={numRondas}
                    onChange={(e) => setNumRondas(Number(e.target.value))}
                    required
                  />
                </div>
                <div className="field" style={{ flex: 1 }}>
                  <label htmlFor="num-jugadores">Jugadores</label>
                  <input
                    id="num-jugadores"
                    type="number"
                    min={3}
                    max={20}
                    value={numJugadores}
                    onChange={(e) => setNumJugadores(Number(e.target.value))}
                    required
                  />
                </div>
                <div className="field" style={{ flex: 1 }}>
                  <label htmlFor="num-infiltrados">Infiltrados</label>
                  <input
                    id="num-infiltrados"
                    type="number"
                    min={1}
                    max={5}
                    value={numInfiltrados}
                    onChange={(e) => setNumInfiltrados(Number(e.target.value))}
                    required
                  />
                </div>
              </div>
              <button
                type="submit"
                className="btn btn-primary btn-block"
                disabled={crearMutation.isPending}
              >
                {crearMutation.isPending ? 'Creando…' : 'Crear sala'}
              </button>
            </form>
          </>
        ) : (
          <>
            <h2>Sala activa</h2>
            <div className="sala-code">{codigoSala}</div>

            {partida && (
              <>
                <div className="text-center mt-1 mb-2">
                  <span className="estado-badge">{partida.estado}</span>
                  <span
                    style={{ color: 'var(--c-text-dim)', fontSize: '0.875rem', marginLeft: '0.75rem' }}
                  >
                    {partida.jugadores.length}/{partida.numJugadores} jugadores
                  </span>
                </div>

                <h3>Jugadores inscritos</h3>
                <div className="player-list">
                  {partida.jugadores
                    .slice()
                    .sort((a, b) => a.ordenTurno - b.ordenTurno)
                    .map((j) => (
                      <div key={j.id} className="player-item">
                        <div
                          className="player-badge"
                          style={{ background: getPlayerColor(j.ordenTurno) }}
                        >
                          {j.ordenTurno}
                        </div>
                        <span className="player-name">{j.nombre}</span>
                      </div>
                    ))}
                </div>
              </>
            )}

            {estadoQuery.isLoading && (
              <p className="text-center mt-2">Cargando sala…</p>
            )}
            {estadoQuery.isError && (
              <div className="alert-error mt-2">Error al cargar los datos de la sala</div>
            )}

            <div className="flex-gap-2 mt-3">
              <button
                className="btn btn-primary"
                style={{ flex: 1 }}
                onClick={irAPartida}
              >
                Ir a la partida →
              </button>
              <button className="btn btn-secondary" onClick={salirDeSala}>
                Salir de sala
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
