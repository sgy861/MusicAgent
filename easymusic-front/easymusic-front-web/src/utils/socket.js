import { ref } from 'vue';
import { mitter } from '@/eventbus/eventBus.js';

const socket = ref(null);
const wsConnected = ref(false);

export const initWebSocket = (token) => {
  if (!token) return;
  if (socket.value && (socket.value.readyState === WebSocket.OPEN || socket.value.readyState === WebSocket.CONNECTING)) {
    return;
  }
  if (socket.value) {
    socket.value.close();
  }

  let wsUrl = `ws://localhost:8099/ws?token=${token}`;
  if (process.env.NODE_ENV === "production") {
    wsUrl = `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}/ws?token=${token}`;
  }

  try {
    socket.value = new WebSocket(wsUrl);

    socket.value.onopen = () => {
      wsConnected.value = true;
      mitter.emit('wsConnected');
      console.log("Global WebSocket connected.");
    };

    socket.value.onmessage = (event) => {
      try {
        const res = JSON.parse(event.data);
        mitter.emit('wsMessage', res);
      } catch (e) {
        console.error("Failed to parse message", e);
      }
    };

    socket.value.onclose = () => {
      wsConnected.value = false;
      mitter.emit('wsDisconnected');
      console.log("Global WebSocket disconnected.");
      // Retry after 5s if token still exists
      setTimeout(() => {
        const curToken = localStorage.getItem('token');
        if (curToken) {
          initWebSocket(curToken);
        }
      }, 5000);
    };
  } catch (error) {
    console.error("WebSocket init failed", error);
  }
};

export const getSocket = () => socket;
export const getWsConnected = () => wsConnected;

export const sendWsMessage = (msg) => {
  if (socket.value && wsConnected.value) {
    socket.value.send(JSON.stringify(msg));
    return true;
  }
  return false;
};

export const closeWebSocket = () => {
  if (socket.value) {
    socket.value.close();
    socket.value = null;
  }
  wsConnected.value = false;
};
