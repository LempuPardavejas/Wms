import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { CartItem, Customer } from '@/types';

interface CartState {
  items: CartItem[];
  customer: Customer | null;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
}

const initialState: CartState = {
  items: [],
  customer: null,
  subtotal: 0,
  taxAmount: 0,
  totalAmount: 0,
};

const calculateTotals = (items: CartItem[]) => {
  const subtotal = items.reduce((sum, item) => sum + item.lineTotal, 0);
  const taxAmount = items.reduce(
    (sum, item) => sum + (item.lineTotal * item.taxRate) / 100,
    0
  );
  const totalAmount = subtotal + taxAmount;

  return { subtotal, taxAmount, totalAmount };
};

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    addToCart: (state, action: PayloadAction<CartItem>) => {
      state.items.push(action.payload);
      const totals = calculateTotals(state.items);
      state.subtotal = totals.subtotal;
      state.taxAmount = totals.taxAmount;
      state.totalAmount = totals.totalAmount;
    },
    removeFromCart: (state, action: PayloadAction<number>) => {
      state.items.splice(action.payload, 1);
      const totals = calculateTotals(state.items);
      state.subtotal = totals.subtotal;
      state.taxAmount = totals.taxAmount;
      state.totalAmount = totals.totalAmount;
    },
    updateCartItem: (state, action: PayloadAction<{ index: number; item: CartItem }>) => {
      state.items[action.payload.index] = action.payload.item;
      const totals = calculateTotals(state.items);
      state.subtotal = totals.subtotal;
      state.taxAmount = totals.taxAmount;
      state.totalAmount = totals.totalAmount;
    },
    setCustomer: (state, action: PayloadAction<Customer | null>) => {
      state.customer = action.payload;
    },
    clearCart: (state) => {
      state.items = [];
      state.customer = null;
      state.subtotal = 0;
      state.taxAmount = 0;
      state.totalAmount = 0;
    },
  },
});

export const { addToCart, removeFromCart, updateCartItem, setCustomer, clearCart } =
  cartSlice.actions;
export default cartSlice.reducer;
