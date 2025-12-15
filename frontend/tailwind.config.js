/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        yt: {
          black: "#0f0f0f",
          gray: "#272727",
          red: "#ff0000",
          text: "#f1f1f1",
        },
      },
    },
  },
  plugins: [],
};
