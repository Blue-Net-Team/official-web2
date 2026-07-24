export interface PosterTemplateConfig {
  id: string
  image: string
  width: number
  height: number
  referralCode: {
    x: number
    y: number
    fontSize: number
    color: string
    fontFamily?: string
    align?: CanvasTextAlign
  }
  qrCode: {
    x: number
    y: number
    size: number
  }
  referrer: {
    x: number
    y: number
    fontSize: number
    color: string
    fontFamily?: string
  }
}

export const POSTER_TEMPLATES: PosterTemplateConfig[] = [
  {
    id: 'default-v1',
    image: '/referral-posters/inf.png',
    width: 1024,
    height: 1536,
    referralCode: {
      x: 513,
      y: 1268,
      fontSize: 72,
      color: '#003d99',
      fontFamily: 'Arial, sans-serif',
      align: 'center',
    },
    qrCode: {
      x: 823,
      y: 1278,
      size: 100,
    },
    referrer: {
      x: 509,
      y: 1469,
      fontSize: 30,
      color: '#003d99',
      fontFamily: 'Arial, sans-serif',
    },
  },
]
