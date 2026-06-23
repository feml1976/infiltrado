const MAX_SIZE_BYTES = 200 * 1024

type ValidationResult = { base64: string; error: null } | { base64: null; error: string }

function detectMimeType(bytes: Uint8Array): string | null {
  if (bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4e && bytes[3] === 0x47) {
    return 'image/png'
  }
  if (bytes[0] === 0xff && bytes[1] === 0xd8 && bytes[2] === 0xff) {
    return 'image/jpeg'
  }
  if (
    bytes[0] === 0x52 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[3] === 0x46 &&
    bytes[8] === 0x57 && bytes[9] === 0x45 && bytes[10] === 0x42 && bytes[11] === 0x50
  ) {
    return 'image/webp'
  }
  return null
}

function bufferToBase64(buffer: ArrayBuffer): string {
  const bytes  = new Uint8Array(buffer)
  const CHUNK  = 8192
  let   result = ''
  for (let i = 0; i < bytes.length; i += CHUNK) {
    result += String.fromCharCode(...bytes.subarray(i, i + CHUNK))
  }
  return btoa(result)
}

export async function validateAndEncodeImage(file: File): Promise<ValidationResult> {
  if (file.size > MAX_SIZE_BYTES) {
    return { base64: null, error: `La imagen supera el límite de 200 KB (actual: ${(file.size / 1024).toFixed(0)} KB)` }
  }

  const buffer = await file.arrayBuffer()
  const bytes  = new Uint8Array(buffer)
  const mime   = detectMimeType(bytes)

  if (!mime) {
    return { base64: null, error: 'Formato no soportado. Usa PNG, JPEG o WEBP.' }
  }

  const base64 = bufferToBase64(buffer)
  return { base64: `data:${mime};base64,${base64}`, error: null }
}
