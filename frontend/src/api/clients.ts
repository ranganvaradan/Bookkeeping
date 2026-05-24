import api from './client'

export type EntityType = 'SOLE_PROP' | 'LLC' | 'S_CORP' | 'C_CORP'

export interface Client {
  id: string
  name: string
  entityType: EntityType
  tenantId: string
  qboRealmId: string | null
  lastSyncAt: string | null
}

export interface CreateClientRequest {
  name: string
  entityType: EntityType
}

export interface QboConnectResponse {
  authUrl: string
}

export interface SyncRequest {
  fromDate: string
  toDate: string
}

export interface SyncResponse {
  jobId: string
}

export async function getClients(): Promise<Client[]> {
  const { data } = await api.get<Client[]>('/api/clients')
  return data
}

export async function getClient(id: string): Promise<Client> {
  const { data } = await api.get<Client>(`/api/clients/${id}`)
  return data
}

export async function createClient(request: CreateClientRequest): Promise<Client> {
  const { data } = await api.post<Client>('/api/clients', request)
  return data
}

export async function connectQbo(clientId: string): Promise<QboConnectResponse> {
  const { data } = await api.get<QboConnectResponse>(`/api/clients/${clientId}/qbo/connect`)
  return data
}

export async function syncClient(clientId: string, request: SyncRequest): Promise<SyncResponse> {
  const { data } = await api.post<SyncResponse>(`/api/clients/${clientId}/sync`, request)
  return data
}
