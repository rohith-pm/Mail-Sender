import Main from './Main/Main.js';
import gmailLogo from './Gmail-logo.png';
function App() {
  return (
    <div className="App" style={{
      display: "flex",
      flexDirection: "row",
      justifyContent: "center",
      alignContent: "center",
      justifyItems: "center",
      alignItems: "center"
    }}>
      <header className="App-header">
      </header>
      <Main />
      {/* <img src={gmailLogo} alt="gmail logo" width="500" height="500px" /> */}
    </div>
  );
}

export default App;
