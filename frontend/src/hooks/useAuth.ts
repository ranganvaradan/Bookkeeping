export function isAuthenticated(): boolean {
  return !!localStorage.getItem('accessToken')
}

export function logout(): void {
  localStorage.removeItem('accessToken')
}
