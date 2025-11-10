import React from 'react'
import {render, screen} from '@testing-library/react'
import {vi} from 'vitest'
import BalanceTrackerUI from './BalanceTrackerUI'

describe('BalanceTrackerUI (JS)', () => {
    beforeEach(() => {
        vi.restoreAllMocks()
        global.fetch = vi.fn()
    })

    it('renders and shows fetched balance', async () => {
        global.fetch.mockResolvedValue({
            ok: true,
            json: async () => 123.45,
        })

        render(<BalanceTrackerUI/>)

        // Shows loading first
        expect(screen.getByText(/\$Loading\.\.\./i)).toBeInTheDocument()

        // Then shows the fetched value
        expect(await screen.findByText('$123.45')).toBeInTheDocument()
        expect(screen.getByText(/Updated:/i)).toBeInTheDocument()
    })

    it('shows error on failed response', async () => {
        global.fetch.mockResolvedValue({
            ok: false,
            status: 500,
            json: async () => ({}),
        })

        render(<BalanceTrackerUI/>)

        // Error message appears
        expect(
            await screen.findByText(/Error: Request failed: 500/i)
        ).toBeInTheDocument()
    })
})
