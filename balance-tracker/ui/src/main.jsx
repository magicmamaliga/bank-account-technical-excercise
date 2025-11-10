import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import BalanceTrackerUI from './BalanceTrackerUI'

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <BalanceTrackerUI/>
    </StrictMode>,
)
