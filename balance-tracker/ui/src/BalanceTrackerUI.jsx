import {useEffect, useState} from 'react'


const ACCOUNT_ID = "ACC-1234567";

function BalanceTrackerUI() {
  const [balance, setBalance] = useState(null);
  const [error, setError] = useState(null);
  const [refreshedTime, setRefreshedTime] = useState(null);

  async function fetchBalance() {
    try {
      const res = await fetch("http://localhost:8080/balance");
      if (!res.ok) throw new Error("Request failed: " + res.status);
      const data = await res.json();
      setBalance(data);
      setError(null);
    } catch (e) {
      setError(e.message);
    }
    setRefreshedTime(new Date().toString());
  }
  
  useEffect(() => {
    fetchBalance(); 
    const id = setInterval(fetchBalance, 3000); 
    return () => clearInterval(id);
  }, []);

  return (
    <>
      <div>
        <h1>Balance Tracker</h1>
        <div>
          <strong>Bank Account Number:</strong> {ACCOUNT_ID}
        </div>
        <div style={{ height: 20 }}>
          <strong>Balance:</strong>
          <span>
            ${balance === null ? "Loading..." : balance}
          </span>
        </div>
      </div>
      <div style={{ height: 20, overflow: "hidden" , marginTop: 40}}>
         {error ? <span >Error: {error}</span> : null}
      </div>
      <div style={{ height: 20, overflow: "hidden" , marginTop: 40}}>
         {refreshedTime ? <span >Updated: {refreshedTime}</span> : null}
      </div>
    </>
  )
}

export default BalanceTrackerUI
