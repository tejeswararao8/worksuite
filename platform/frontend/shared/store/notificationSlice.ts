import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface NotificationState {
  unreadCount: number;
}

const notificationSlice = createSlice({
  name: 'notification',
  initialState: { unreadCount: 0 } as NotificationState,
  reducers: {
    setUnreadCount(state, action: PayloadAction<number>) {
      state.unreadCount = action.payload;
    },
    decrementUnread(state) {
      state.unreadCount = Math.max(0, state.unreadCount - 1);
    },
    clearUnread(state) {
      state.unreadCount = 0;
    },
  },
});

export const { setUnreadCount, decrementUnread, clearUnread } = notificationSlice.actions;
export default notificationSlice.reducer;
