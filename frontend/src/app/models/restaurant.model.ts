export interface RestaurantDto {
  id?: string;
  name: string;
  address: string;
  phone: string;
  website: string;
  schedule: string;
  /** Optional photo URL; when absent the UI falls back to a curated stock image. */
  imageUrl?: string;
}
