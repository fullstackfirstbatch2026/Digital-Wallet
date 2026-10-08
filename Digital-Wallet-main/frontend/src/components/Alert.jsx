/** Bootstrap alert. type = success | danger | warning | info */
export default function Alert({ type = 'info', message, onClose }) {
  if (!message) return null;
  return (
    <div className={`alert alert-${type} alert-dismissible`} role="alert">
      {message}
      {onClose && <button type="button" className="btn-close" aria-label="Close" onClick={onClose} />}
    </div>
  );
}
