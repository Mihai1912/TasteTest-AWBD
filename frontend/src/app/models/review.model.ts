export interface ReviewDto {
  comment: string;
  rating: number;
  urserName: string;
  createdAt?: string;
}

export interface ReviewIdDto extends ReviewDto {
  id: string;
}
