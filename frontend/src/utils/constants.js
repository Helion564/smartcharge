export const CONNECTOR_TYPES = [
  { value: 'CCS', label: 'CCS (Combined Charging System)' },
  { value: 'CHADEMO', label: 'CHAdeMO' },
  { value: 'TYPE2', label: 'Type 2 AC' },
  { value: 'TYPE1', label: 'Type 1 AC' },
  { value: 'GB_T', label: 'GB/T (China Standard)' },
  { value: 'BHARAT_AC001', label: 'Bharat AC-001' },
  { value: 'BHARAT_DC001', label: 'Bharat DC-001' },
  { value: 'TESLA', label: 'Tesla Supercharger' },
]

export const POWER_LEVELS = [
  { value: 'LEVEL_1', label: 'Level 1 (1.4 - 3.3 kW)' },
  { value: 'LEVEL_2', label: 'Level 2 (7.2 - 22 kW)' },
  { value: 'DC_FAST', label: 'DC Fast (50 kW)' },
  { value: 'ULTRA_FAST', label: 'Ultra Fast (120 - 150 kW)' },
  { value: 'HYPER_FAST', label: 'Hyper Fast (250 - 350 kW)' },
]

export const STATUS_COLORS = {
  AVAILABLE: 'bg-emerald-500 text-white',
  OCCUPIED: 'bg-amber-500 text-white',
  OUT_OF_ORDER: 'bg-red-500 text-white',
  RESERVED: 'bg-blue-500 text-white',
}
