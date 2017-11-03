class Colours {

  static _colours = [
    "1BAB39", // green
    "7D41AB", // purple
    "FF9700", // orange
    "3F1CE8", // blue
    "FFE000", // yellow
    "FF0216", // red
    "E84DB9", // pink
    "32DBE3" // cyan
  ]

  constructor() {
    this.index = 0
    this.length = Colours._colours.length
  }

  getNextColour() {
    if(this.index === this.length) {
      this.index = 0;
    }

    return Colours._colours[this.index++]
  }
}

export default Colours;
