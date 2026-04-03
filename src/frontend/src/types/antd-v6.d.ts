import type { CardProps } from 'antd/es/card/Card'
import type { ForwardRefExoticComponent, RefAttributes } from 'react'

type CardComponent = ForwardRefExoticComponent<CardProps & RefAttributes<HTMLDivElement>> & {
  Grid: React.FC
  Meta: React.FC
}

declare module 'antd/es/card' {
  // eslint-disable-next-line @typescript-eslint/no-empty-object-type
  export interface CardInterface extends CardComponent {}
}

declare module 'antd' {
  export const Card: CardComponent
}
