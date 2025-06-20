import * as React from "react"
import { Check, ChevronsUpDown } from "lucide-react"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"

import {
  LocationDTO,
  searchLocations,
} from "@/features/todo/services/flightSearchService"

interface ComboBoxAutocompleteProps {
  value: string
  onValueChange: (value: string) => void
  placeholder?: string
  label?: string
  subType?: "AIRPORT" | "CITY"
}

function ComboBoxAutocomplete({
  value,
  onValueChange,
  placeholder = "Search...",
  label,
  subType = "AIRPORT",
}: ComboBoxAutocompleteProps) {
  const [open, setOpen] = React.useState(false)
  const [input, setInput] = React.useState("")
  const [options, setOptions] = React.useState<LocationDTO[]>([])
  const [selectedOption, setSelectedOption] = React.useState<LocationDTO | null>(null)
  const [isLoading, setIsLoading] = React.useState(false)

  React.useEffect(() => {
    const fetchSelectedOption = async () => {
      if (value && !selectedOption) {
        try {
          const res = await searchLocations({
            subType,
            keyword: value,
            pageLimit: 1,
            view: "LIGHT",
          })
          if (res.data.length > 0) {
            setSelectedOption(res.data[0])
          }
        } catch (err) {
          console.error("Error fetching selected option:", err)
        }
      } else if (!value) {
        setSelectedOption(null)
      }
    }

    fetchSelectedOption()
  }, [value, subType])

  React.useEffect(() => {
    const handler = setTimeout(async () => {
      const keyword = input.trim()
      if (keyword.length >= 2) {
        setIsLoading(true)
        try {
          console.log("Searching locations with:", { subType, keyword })
          const res = await searchLocations({
            subType,
            keyword: keyword.toUpperCase(),
            pageLimit: 10,
            view: "LIGHT",
          })
          setOptions(res.data)
        } catch (err) {
          console.error("Error searching locations:", err)
          setOptions([])
        } finally {
          setIsLoading(false)
        }
      } else {
        setOptions([])
      }
    }, 400)

    return () => clearTimeout(handler)
  }, [input, subType])

  const displayText = selectedOption
    ? `${selectedOption.iataCode} - ${selectedOption.name}`
    : value

  return (
    <div className="flex flex-col space-y-1.5">
      {label && <label className="text-sm font-medium">{label}</label>}
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            role="combobox"
            aria-expanded={open}
            className="w-full justify-between"
          >
            {value ? (
              <span>{displayText}</span>
            ) : (
              <span className="text-muted-foreground">{placeholder}</span>
            )}
            <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
          </Button>
        </PopoverTrigger>
        <PopoverContent 
          className="w-full p-0" 
          align="start"
          side="bottom"
          sideOffset={5}
        >
          <Command shouldFilter={false}>
            <CommandInput
              autoFocus
              placeholder={placeholder}
              value={input}
              onValueChange={setInput}
              className="h-9"
            />
            <CommandList>
              <CommandEmpty className="py-6 text-center text-sm">
                {isLoading ? "Searching..." : "No results"}
              </CommandEmpty>
              <CommandGroup>
                {options.map((opt) => (
                  <CommandItem
                    key={opt.iataCode}
                    value={opt.iataCode}
                    onSelect={(currentValue) => {
                      const newValue = currentValue === value ? "" : currentValue.toUpperCase()
                      onValueChange(newValue)
                      setSelectedOption(newValue ? opt : null)
                      setOpen(false)
                      setInput("")
                    }}
                    className="flex items-center justify-between"
                  >
                    <span>{opt.iataCode} - {opt.name}</span>
                    <Check
                      className={cn(
                        "ml-auto h-4 w-4",
                        value === opt.iataCode ? "opacity-100" : "opacity-0"
                      )}
                    />
                  </CommandItem>
                ))}
              </CommandGroup>
            </CommandList>
          </Command>
        </PopoverContent>
      </Popover>
    </div>
  )
}

export default ComboBoxAutocomplete
